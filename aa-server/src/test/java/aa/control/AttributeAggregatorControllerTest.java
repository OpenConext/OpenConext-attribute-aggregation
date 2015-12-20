package aa.control;

import aa.AbstractIntegrationTest;
import aa.model.*;
import aa.util.StreamUtils;
import org.junit.Test;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static aa.util.StreamUtils.listFromIterable;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=no-csrf", "attribute.authorities.config.path=classpath:testAttributeAuthorities.yml"})
public class AttributeAggregatorControllerTest extends AbstractIntegrationTest {

  @Override
  protected boolean isBasicAuthenticated() {
    return true;
  }

  @Test
  public void testAttributeAggregate() throws Exception {
    List<UserAttribute> result = doAttributeAggregate("http://mock-sp");

    assertEquals(1, result.size());

    UserAttribute userAttribute = result.get(0);
    assertEquals("urn:mace:dir:attribute-def:eduPersonOrcid", userAttribute.getName());
    assertEquals(singletonList("urn:x-surfnet:aa1:test"), userAttribute.getValues());
    assertEquals("aa1", userAttribute.getSource());
  }

  @Test
  public void testNoServiceProvider() throws Exception {
    List<UserAttribute> result = doAttributeAggregate("http://unknown-sp");

    assertEquals(0, result.size());
  }

  @SuppressWarnings("unchecked")
  private List<UserAttribute> doAttributeAggregate(String serviceProviderEntityId) throws URISyntaxException {
    UserAttribute input = new UserAttribute("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified",
        singletonList("urn:collab:person:example.com:admin"),
        null);
    UserAttributes userAttributes = new UserAttributes(serviceProviderEntityId, singletonList(input));

    RequestEntity requestEntity = new RequestEntity(userAttributes, headers, HttpMethod.POST, new URI("http://localhost:" + port + "/aa/api/attribute/aggregate"));
    ResponseEntity<List<UserAttribute>> response = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<List<UserAttribute>>() {
    });

    return response.getBody();
  }

}