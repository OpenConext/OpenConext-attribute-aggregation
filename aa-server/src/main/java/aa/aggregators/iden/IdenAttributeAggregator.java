package aa.aggregators.iden;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

public class IdenAttributeAggregator extends AbstractAttributeAggregator {

  public IdenAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
    super(attributeAuthorityConfiguration);
  }

  @Override
  public List<UserAttribute> aggregate(List<UserAttribute> input) {
    String unspecifiedId = getUserAttributeSingleValue(input, NAME_ID);
    URI uri = uri(unspecifiedId);
    Map<String, Object> body = getUserMap(uri);

    LOG.debug("Retrieved iDEN with request: {} and response: {}", uri, body);

    if (body.isEmpty()) {
      //404
      return new ArrayList<>();
    }

    String email = (String) body.get("email");
    String affiliations = (String) body.get("affiliations");
    List<UserAttribute> userAttributes = new ArrayList<>();

    if (email != null) {
      userAttributes.add(new UserAttribute(EMAIL, singletonList(email)));
    }
    if (affiliations != null) {
      userAttributes.add(new UserAttribute(EDU_PERSON_AFFILIATION, asList(affiliations.split(", "))));
    }
    userAttributes.add(new UserAttribute(IS_MEMBER_OF, singletonList("surf.nl")));

    return userAttributes;
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getUserMap(URI uri) {
    try {
      return getRestTemplate().getForEntity(uri, Map.class).getBody();
    } catch (HttpClientErrorException e) {
      if (e.getStatusCode().value() != 404) {
        throw e;
      }
      return Collections.EMPTY_MAP;
    }
  }

  private URI uri(String unspecifiedId) {
    try {
      return new URI(getAttributeAuthorityConfiguration().getEndpoint().concat("/").concat(unspecifiedId));
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }
}
