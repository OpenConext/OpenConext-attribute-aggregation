package aa.aggregators.voot;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class VootAttributeAggregator extends AbstractAttributeAggregator {

    private final OAuth2RestTemplate vootService;

    public VootAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration,
                                   String authorizationAccessTokenUrl) {
        super(attributeAuthorityConfiguration);
        this.vootService = vootRestTemplate(attributeAuthorityConfiguration, authorizationAccessTokenUrl);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<UserAttribute> aggregate(List<UserAttribute> input) {
        String userId = getUserAttributeSingleValue(input, NAME_ID);
        String url = endpoint() + "/internal/groups/{userUrn}";
        List<Map<String, Object>> listOfGroupMaps = (List<Map<String, Object>>) vootService.getForObject(url, List.class, userId);
        List<String> groups = listOfGroupMaps.stream().map(entry -> (String) entry.get("id")).collect(toList());
        LOG.debug("Retrieved VOOT groups with request: {} and response: {}", url, groups);
        return mapValuesToUserAttribute(IS_MEMBER_OF, groups);
    }

    private OAuth2RestTemplate vootRestTemplate(AttributeAuthorityConfiguration configuration,
                                                String authorizationAccessTokenUrl) {
        ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
        details.setId("aa");
        details.setClientId(configuration.getUser());
        details.setClientSecret(configuration.getPassword());
        details.setAccessTokenUri(authorizationAccessTokenUrl);
        details.setScope(singletonList("groups"));
        return new OAuth2RestTemplate(details);
    }
}
