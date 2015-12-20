package aa.aggregators;

import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static aa.util.StreamUtils.singletonOptionalCollector;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class TestingAttributeAggregator extends AbstractAttributeAggregator {

  private final boolean useEndpoint;
  private List<String> attributeKeys = Collections.singletonList("urn:mace:dir:attribute-def:eduPersonPrincipalName");

  public TestingAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration,
                                    boolean useEndpoint) {
    super(attributeAuthorityConfiguration);
    this.useEndpoint = useEndpoint;
  }

  @Override
  public List<UserAttribute> aggregate(List<UserAttribute> input) {
    if (useEndpoint) {
      return getRestTemplate().exchange(new RequestEntity(HttpMethod.GET, endpoint()),
          new ParameterizedTypeReference<List<UserAttribute>>() {
          }).getBody();
    } else {
      AttributeAuthorityConfiguration authority = getAttributeAuthorityConfiguration();
      return authority.getAttributes().stream()
          .map(attribute -> new UserAttribute(
              attribute.getName(),
              singletonList("urn:x-surfnet:" + getAttributeAuthorityId() + ":test"),
              authority.getId()))
          .collect(toList());
    }
  }

  @Override
  public List<String> attributeKeysRequired() {
    return attributeKeys;
  }
}
