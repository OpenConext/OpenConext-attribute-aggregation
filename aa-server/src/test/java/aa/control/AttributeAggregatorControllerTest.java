package aa.control;

import aa.AbstractIntegrationTest;
import aa.model.UserAttribute;
import aa.model.UserAttributes;
import org.junit.Test;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=no-csrf,aa-test", "attribute.authorities.config.path=classpath:testAttributeAuthorities.yml"})
public class AttributeAggregatorControllerTest extends AbstractIntegrationTest {

  @Override
  protected boolean isBasicAuthenticated() {
    return true;
  }

  @Test
  public void testAttributeAggregateWithoutRequiredAttribute() throws Exception {
    List<UserAttribute> result = doAttributeAggregate("http://mock-sp", "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");

    assertEquals(0, result.size());
  }
  @Test
  public void testAttributeAggregateRequiredAttributePresent() throws Exception {
    List<UserAttribute> result = doAttributeAggregate("http://mock-sp", "urn:mace:dir:attribute-def:eduPersonPrincipalName");

    assertEquals(1, result.size());

    UserAttribute userAttribute = result.get(0);
    assertEquals("urn:mace:dir:attribute-def:eduPersonOrcid", userAttribute.getName());
    assertEquals(singletonList("urn:x-surfnet:aa1:test"), userAttribute.getValues());
    assertEquals("aa1", userAttribute.getSource());
  }


  @Test
  public void testNoServiceProvider() throws Exception {
    List<UserAttribute> result = doAttributeAggregate("http://unknown-sp", "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified");

    assertEquals(0, result.size());
  }

  @SuppressWarnings("unchecked")
  private List<UserAttribute> doAttributeAggregate(String serviceProviderEntityId, String userAttributeName) throws URISyntaxException {
    UserAttribute input = new UserAttribute(userAttributeName,
        singletonList("urn:collab:person:example.com:admin"),
        null);
    UserAttributes userAttributes = new UserAttributes(serviceProviderEntityId, singletonList(input));

    RequestEntity requestEntity = new RequestEntity(userAttributes, headers, HttpMethod.POST, new URI("http://localhost:" + port + "/aa/api/attribute/aggregate"));
    ResponseEntity<List<UserAttribute>> response = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<List<UserAttribute>>() {
    });

    return response.getBody();
  }

}