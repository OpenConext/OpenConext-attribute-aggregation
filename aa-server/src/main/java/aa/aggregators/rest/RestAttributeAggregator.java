package aa.aggregators.rest;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.ArpValue;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.PathParam;
import aa.model.UserAttribute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
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

    private List<UserAttribute> mapToAttributes(String data, AttributeAuthorityConfiguration configuration) {
        List<UserAttribute> result = new ArrayList<>();

        if (null == configuration.getMappings() || configuration.getMappings().isEmpty()) {
            LOG.warn("No configured mappings found for retrieved data from REST endpoint, returning empty enriched attribute list");
            return result;
        }

        JsonNode node;
        try {
            node = objectMapper.readTree(data);
        } catch (JsonProcessingException exception) {
            LOG.warn("Can not parse response from REST endpoint, returning empty enriched attribute list");
            exception.printStackTrace();
            return result;
        }

        configuration.getMappings().forEach(mapping -> {
            JsonNode jsonNode = node.findValue(mapping.getResponseKey());
            if (null != jsonNode) {
                result.add(createAttribute(configuration.getId(), mapping.getTargetAttribute(), jsonNode.asText()));
            }
        });

        return result;
    }

    private UserAttribute createAttribute(String sourceId, String key, String value) {
        UserAttribute attribute = new UserAttribute();
        attribute.setName(key);
        attribute.setValues(List.of(value));
        attribute.setSource(sourceId);
        return attribute;
    }

}
