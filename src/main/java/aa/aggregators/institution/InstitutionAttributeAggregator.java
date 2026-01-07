package aa.aggregators.institution;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.ArpValue;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class InstitutionAttributeAggregator extends AbstractAttributeAggregator {

    private final Map<String, InstitutionEndpoint> institutionServicesConfig;
    private final SAMLMapping samlMapping = new SAMLMapping();

    @SneakyThrows
    public InstitutionAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration,
                                          Resource serviceProviderConfigPath,
                                          ObjectMapper objectMapper) {
        super(attributeAuthorityConfiguration);
        Map<String, Map<String, String>> config = objectMapper.readValue(serviceProviderConfigPath.getInputStream(), new TypeReference<>() {
        });
        this.institutionServicesConfig = config
                .entrySet()
                .stream()
                .collect(Collectors.toMap(entry -> entry.getKey(), entry ->
                        new InstitutionEndpoint(entry.getValue().get("baseUrl"),
                                entry.getValue().get("userName"),
                                entry.getValue().get("password"))));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<UserAttribute> aggregate(List<UserAttribute> input, Map<String, List<ArpValue>> arpAttributes) {
        //This is by contract. The spEntityID is always present
        String spEntityID = input.stream().filter(userAttribute -> userAttribute.getName().equals(SP_ENTITY_ID))
                .findFirst().get().getValues().getFirst();
        InstitutionEndpoint institutionEndpoint = institutionServicesConfig.get(spEntityID);
        if (institutionEndpoint == null) {
            LOG.error("No InstitutionEndpoint configured for: {}", spEntityID);
            return errorResponse(input);
        }
        RestTemplate restTemplate = super.initializeRestTemplate(super.getAttributeAuthorityConfiguration());
        BasicAuthenticationInterceptor interceptor = new BasicAuthenticationInterceptor(
                institutionEndpoint.getUserName(),
                institutionEndpoint.getPassword());
        restTemplate.getInterceptors().add(interceptor);

        //This is by contract. The eduID attribute is present, otherwise this aggregator is not called
        String eduID = input.stream().filter(attribute -> attribute.getName().equals(EDU_ID))
                .findFirst().get().getValues().getFirst();
        String url = String.format("%s/api/attributes/%s", removeTrailingSlash(institutionEndpoint.getBaseURL()), eduID);
        //Use try / catch, and in case of error, default to the input user attributes.
        //See https://github.com/OpenConext/OpenConext-attribute-aggregation/issues/144
        Map<String, List<String>> body;
        try {
            body = restTemplate.getForEntity(url, Map.class).getBody();
        } catch (HttpStatusCodeException e) {
            String msg = String.format("InstitutionEndpoint %s configured for: %s, returned an error %s",
                    institutionEndpoint,
                    spEntityID,
                    e.getStatusCode());
            // A 404 if the user wasn't found is not an error
            if (e.getStatusCode().equals(HttpStatus.NOT_FOUND)) {
                LOG.info(msg);
            } else {
                LOG.error(msg, e);
            }
            return errorResponse(input);
        }

        LOG.debug("Received response {} from {} for SP {}", body, institutionEndpoint.getBaseURL(), spEntityID);

        //Get all attribute names that are configured in the ARP for this aggregator
        List<String> institutionAttributeNames = arpAttributes.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(arpValue -> arpValue.getSource().equals(super.getAttributeAuthorityId())))
                .map(entry -> entry.getKey())
                .toList();
        //Must be mutable
        List<UserAttribute> result = new ArrayList<>();
        //Get all the values (if present) from the institution response for all ARP names
        institutionAttributeNames.forEach(samlAttributeName -> {
            Optional<String> optionalName = samlMapping.convertSAMLAttributeName(samlAttributeName);
            if (optionalName.isPresent()) {
                List<String> values = body.get(optionalName.get());
                if (!CollectionUtils.isEmpty(values)) {
                    result.add(new UserAttribute(samlAttributeName, values, super.getAttributeAuthorityId()));
                } else {
                    //Look up the value of the attribute from the EB input and if present, use that as the return value
                    input.stream()
                            .filter(userAttribute -> userAttribute.getName().equals(samlAttributeName))
                            .findFirst()
                            .ifPresent(userAttribute -> result.add(
                                    new UserAttribute(
                                            samlAttributeName, userAttribute.getValues(), super.getAttributeAuthorityId()))
                            );
                }
            }
        });
        return result;
    }

    private String removeTrailingSlash(String baseURL) {
        if (baseURL.endsWith("/")) {
            return baseURL.substring(0, baseURL.length() - 1);
        }
        return baseURL;
    }

    private List<UserAttribute> errorResponse(List<UserAttribute> userAttributesInput) {
        //We must add the correct source of the attribute, otherwise it is filtered out in the upstream stream
        return userAttributesInput.stream()
                .map(userAttribute -> new UserAttribute(
                        userAttribute.getName(),
                        userAttribute.getValues(),
                        super.getAttributeAuthorityId()
                ))
                .toList();
    }
}
