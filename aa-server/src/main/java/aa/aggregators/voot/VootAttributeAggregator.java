package aa.aggregators.voot;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class VootAttributeAggregator extends AbstractAttributeAggregator {

  private final OAuth2RestTemplate vootService;

  public VootAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration,
                                 String authorizationAccessTokenUrl) {
    super(attributeAuthorityConfiguration);
    this.vootService = vootRestTemplate(attributeAuthorityConfiguration, authorizationAccessTokenUrl);
  }

  @Override
  public List<UserAttribute> aggregate(List<UserAttribute> input) {
    String userId = getUserAttributeSingleValue(input, NAME_ID);
    String url = endpoint() + "/internal/groups/{userUrn}";
    List<Map<String, Object>> listOfGroupMaps = (List<Map<String, Object>>) vootService.getForObject(url, List.class, userId);
    List<String> groups = listOfGroupMaps.stream().map(entry -> (String) entry.get("id")).collect(toList());
    LOG.debug("Retrieved groups: {}", groups);
    return mapValuesToUserAttribute(GROUP, groups);
  }

  private OAuth2RestTemplate vootRestTemplate(AttributeAuthorityConfiguration configuration,
                                              String authorizationAccessTokenUrl) {
    ClientCredentialsResourceDetails details = new ClientCredentialsResourceDetails();
    details.setId("aa");
    details.setClientId(configuration.getUser());
    details.setClientSecret(configuration.getPassword());
    details.setAccessTokenUri(authorizationAccessTokenUrl);
    details.setScope(asList("groups"));
    return new OAuth2RestTemplate(details);
  }
}