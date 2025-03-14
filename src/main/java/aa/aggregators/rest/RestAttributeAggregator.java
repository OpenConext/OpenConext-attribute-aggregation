package aa.aggregators.rest;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class RestAttributeAggregator extends AbstractAttributeAggregator implements Runnable {

    private static final String emptyResponse = "[]";

    private final ObjectMapper objectMapper;

    boolean cacheUsed = false;

    private String cacheData;

    public RestAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        super(attributeAuthorityConfiguration);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<UserAttribute> aggregate(List<UserAttribute> input, Map<String, List<ArpValue>> arpAttributes) {
        cacheUsed = false;
        AttributeAuthorityConfiguration configuration = getAttributeAuthorityConfiguration();
        String data = fetchData(input, configuration);
        return mapToAttributes(data, configuration, input);
    }

    @Override
    public void run() {
        if (cachingEnabled()) {
            AttributeAuthorityConfiguration configuration = getAttributeAuthorityConfiguration();
            LOG.debug("Refreshing cache for REST aggregator {}", configuration.getId());
            refreshCache(configuration);
        }
    }

    private String fetchData(List<UserAttribute> attributes, AttributeAuthorityConfiguration configuration) {
        try {
            return doRequest(configuration.getHeaders(), configuration.getUser(), configuration.getPassword(),
                    configuration.getEndpoint(), configuration.getPathParams(), attributes,
                    configuration.getRequestParams(), configuration.getRequestMethod()).getBody();
        } catch (HttpStatusCodeException exception) {
            LOG.warn("Exception occurred during data retrieval for REST aggregator {}:", configuration.getId(), exception);
            if (!exception.getResponseBodyAsString().isEmpty()) {
                LOG.warn("Response body found for exception, returning response body");
                return exception.getResponseBodyAsString();
            }
            return handleException(exception, configuration);
        } catch (RestClientException exception) {
            LOG.warn("Exception occurred during data retrieval for REST aggregator {}:", configuration.getId(), exception);
            return handleException(exception, configuration);
        }
    }

    private ResponseEntity<String> doRequest(List<Header> configHeaders, String user, String password, String endpoint, List<PathParam> pathParams,
                                             List<UserAttribute> attributes, List<RequestParam> requestParams, String requestMethod) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (null != configHeaders) {
            configHeaders.forEach(header -> headers.add(header.getKey(), header.getValue()));
        }
        if (user != null && password != null) {
            headers.setBasicAuth(user, password);
        }
        // Process path parameters
        if (null != pathParams) {
            pathParams.sort(Comparator.comparing(PathParam::getIndex));
            Object[] pathParamValues = pathParams.stream()
                    .map(pathParam -> attributes.stream()
                            .filter(attribute -> attribute.getName().equals(pathParam.getSourceAttribute()))
                            .map(attribute -> attribute.getValues().get(0))
                            .collect(Collectors.toList())
                    )
                    .flatMap(List::stream).toArray();
            endpoint = String.format(endpoint, pathParamValues);
        }
        // Process request parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(endpoint);
        if (null != requestParams) {
            requestParams.forEach(requestParam -> attributes.stream()
                    .filter(attribute -> attribute.getName().equals(requestParam.getSourceAttribute())).findFirst()
                    .ifPresent(param -> builder.queryParam(requestParam.getName(), param.getValues().get(0)))
            );
        }

        HttpMethod method = HttpMethod.valueOf(requestMethod);
        if (null == method) {
            LOG.info("Can not resolve unknown HTTP method: {}, defaulting to GET", requestMethod);
            method = HttpMethod.GET;
        }

        return getRestTemplate().exchange(builder.toUriString(), method,
                new HttpEntity<>(null, headers), new ParameterizedTypeReference<>() {});
    }

    private List<UserAttribute> mapToAttributes(String data, AttributeAuthorityConfiguration configuration, List<UserAttribute> input) {
        if (CollectionUtils.isEmpty(configuration.getMappings())) {
            throw new IllegalArgumentException("No configured mappings found for retrieved data from REST endpoint, returning empty enriched attribute list");
        }

        List<UserAttribute> result = new ArrayList<>();
        List<Map<String, Object>> rootList = searchRootElement(data, configuration, input);

        if (rootList.isEmpty() && Boolean.TRUE.equals(configuration.getHandleResponseErrorAsEmpty())) {
            configuration.getMappings().forEach(mapping ->
                    result.add(createAttribute(configuration.getId(), mapping.getTargetAttribute(), ""))
            );
        } else {
            rootList.forEach(m -> configuration.getMappings().forEach(mapping -> {
                // Check if filter is present and applies
                MappingFilter filter = mapping.getFilter();
                if (null != filter && StringUtils.hasText(filter.getKey()) && !filter.getValue().equals(m.get(filter.getKey()))) {
                    return;
                }
                Object o = m.get(mapping.getResponseKey());
                if (o != null) {
                    result.add(createAttribute(configuration.getId(), mapping.getTargetAttribute(), o.toString()));
                }
            }));
        }
        // Make sure that all values with the same name are grouped together
        Map<String, List<UserAttribute>> groupedBy = result.stream().collect(Collectors.groupingBy(UserAttribute::getName));
        return groupedBy.entrySet().stream().map(entry ->
                new UserAttribute(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(UserAttribute::getValues)
                                .flatMap(Collection::stream)
                                .sorted()
                                .toList(),
                        configuration.getId())
        ).collect(Collectors.toList());
    }

    /**
     * Search for relevant root element from the provided data. Data is handled as JSON string to which JSONPath mapping
     * is applied. In case cache data is used then apply mapping and filtering defined in cache configuration.
     *
     * @param data Data retrieved from REST API
     * @param configuration Relevant attribute authority configuration
     * @param input Input attributes to apply filtering to cache data if required
     * @return List of relevant records from data
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> searchRootElement(String data, AttributeAuthorityConfiguration configuration, List<UserAttribute> input) {
        List<Map<String, Object>> rootList;
        try {
            Object obj = objectMapper.readValue(data, Object.class);

            // Cache can contain irrelevant records, apply filter to search for relevant records
            if (cacheUsed) {
                String cacheRootList = configuration.getCache().getRootListName();
                List<CacheFilter> cacheFilters = configuration.getCache().getFilters();
                if (null != cacheFilters && !cacheFilters.isEmpty()) {
                    cacheFilters.sort(Comparator.comparing(CacheFilter::getIndex));
                    for (CacheFilter cacheFilter : cacheFilters) {
                        String filterKey = cacheFilter.getKey();
                        UserAttribute filterAttribute = input.stream().filter(attribute ->
                                attribute.getName().equals(cacheFilter.getSourceAttribute())).findFirst().orElse(null);
                        String filterValue = null != filterAttribute ? filterAttribute.getValues().get(0) : null;

                        if (null != filterValue) {
                            cacheRootList = String.format(cacheRootList, filterKey, filterValue);
                        }
                    }
                }

                obj = JsonPath.from(data).get(cacheRootList);
            } else {
                // Cache data not used, apply normal root element mapping
                if (StringUtils.hasText(configuration.getRootListName())) {
                    obj = JsonPath.from(data).get(configuration.getRootListName());
                }
            }

            if (obj instanceof List) {
                rootList = (List<Map<String, Object>>) obj;
            } else {
                rootList = List.of((Map<String, Object>) obj);
            }
        } catch (JsonProcessingException | IllegalArgumentException | NullPointerException exception) {
            LOG.warn("Can not parse response from REST endpoint of {}, treating as empty response", configuration.getId(), exception);
            return Collections.emptyList();
        }

        return rootList;
    }

    private UserAttribute createAttribute(String sourceId, String key, String value) {
        UserAttribute attribute = new UserAttribute();
        attribute.setName(key);
        attribute.setValues(List.of(value));
        attribute.setSource(sourceId);
        return attribute;
    }

    private void refreshCache(AttributeAuthorityConfiguration configuration) {
        try {
            Cache cache = configuration.getCache();
            this.cacheData = doRequest(cache.getHeaders(), configuration.getUser(), configuration.getPassword(),
                    cache.getEndpoint(), null, null, null, cache.getRequestMethod())
                    .getBody();
        } catch (RestClientException exception) {
            LOG.warn("Failed to retrieve cache data", exception);
        }
    }

    private String handleException(RestClientException exception, AttributeAuthorityConfiguration configuration) {
        if (cachingEnabled()) {
            LOG.info("Cache configuration found and enabled, returning cache data");
            cacheUsed = true;
            return cacheData;
        }
        if (Boolean.TRUE.equals(configuration.getHandleResponseErrorAsEmpty())) {
            LOG.info("Exception thrown but configured to handle as empty data, returning empty data");
            return emptyResponse;
        }
        throw exception;
    }

}
