package aa.aggregators.rest;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.path.json.JsonPath;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

public class RestAttributeAggregator extends AbstractAttributeAggregator {

    private final ObjectMapper objectMapper;

    public RestAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        super(attributeAuthorityConfiguration);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public List<UserAttribute> aggregate(List<UserAttribute> input, Map<String, List<ArpValue>> arpAttributes) {
        AttributeAuthorityConfiguration configuration = getAttributeAuthorityConfiguration();
        String data = fetchData(input, configuration);
        return mapToAttributes(data, configuration);
    }

    private String fetchData(List<UserAttribute> attributes, AttributeAuthorityConfiguration configuration) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (null != configuration.getHeaders()) {
            configuration.getHeaders().forEach(header -> headers.add(header.getKey(), header.getValue()));
        }
        if (configuration.getUser() != null && configuration.getPassword() != null) {
            headers.setBasicAuth(configuration.getUser(), configuration.getPassword());
        }
        // Process path parameters
        String endpoint = configuration.getEndpoint();
        if (null != configuration.getPathParams()) {
            configuration.getPathParams().sort(Comparator.comparing(PathParam::getIndex));
            Object[] pathParamValues = configuration.getPathParams().stream()
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
        if (null != configuration.getRequestParams()) {
            configuration.getRequestParams().forEach(requestParam -> attributes.stream()
                    .filter(attribute -> attribute.getName().equals(requestParam.getSourceAttribute())).findFirst()
                    .ifPresent(param -> builder.queryParam(requestParam.getName(), param.getValues().get(0)))
            );
        }

        HttpMethod method = HttpMethod.resolve(configuration.getRequestMethod());
        if (null == method) {
            LOG.info("Can not resolve unknown HTTP method: {}, defaulting to GET", configuration.getRequestMethod());
            method = HttpMethod.GET;
        }
        return getRestTemplate().exchange(builder.toUriString(), method,
                new HttpEntity<>(null, headers), new ParameterizedTypeReference<String>() {
                }).getBody();
    }

    @SuppressWarnings("unchecked")
    private List<UserAttribute> mapToAttributes(String data, AttributeAuthorityConfiguration configuration) {
        if (CollectionUtils.isEmpty(configuration.getMappings())) {
            throw new IllegalArgumentException("No configured mappings found for retrieved data from REST endpoint, returning empty enriched attribute list");
        }

        List<UserAttribute> result = new ArrayList<>();
        List<Map<String, Object>> rootList;
        try {
            Object obj = objectMapper.readValue(data, Object.class);
            if (StringUtils.hasText(configuration.getRootListName())) {
                obj = JsonPath.from(data).get(configuration.getRootListName());
            }

            if (obj instanceof List) {
                rootList = (List<Map<String, Object>>) obj;
            } else {
                rootList = List.of((Map<String, Object>) obj);
            }
        } catch (JsonProcessingException exception) {
            LOG.error("Can not parse response from REST endpoint, returning empty enriched attribute list", exception);
            return Collections.emptyList();
        }
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
        // Make sure that all values with the same name are grouped together
        Map<String, List<UserAttribute>> groupedBy = result.stream().collect(Collectors.groupingBy(UserAttribute::getName));
        return groupedBy.entrySet().stream().map(entry ->
                new UserAttribute(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(UserAttribute::getValues)
                                .flatMap(Collection::stream)
                                .collect(Collectors.toList()),
                        configuration.getId())
        ).collect(Collectors.toList());
    }

    private UserAttribute createAttribute(String sourceId, String key, String value) {
        UserAttribute attribute = new UserAttribute();
        attribute.setName(key);
        attribute.setValues(List.of(value));
        attribute.setSource(sourceId);
        return attribute;
    }

}
