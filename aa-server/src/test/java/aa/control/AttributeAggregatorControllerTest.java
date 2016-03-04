package aa.control;

import aa.AbstractIntegrationTest;
import aa.model.UserAttribute;
import org.junit.Test;
import org.springframework.boot.test.WebIntegrationTest;

import java.util.List;

import static java.util.Collections.singletonList;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=no-csrf,aa-test", "attribute.authorities.config.path=classpath:testAttributeAuthorities.yml"})
public class AttributeAggregatorControllerTest extends AbstractIntegrationTest {

  @Override
  protected boolean isBasicAuthenticated() {
    return true;
  }

  @Test
  public void testAttributeAggregateWithoutRequiredAttribute() throws Exception {
    List<UserAttribute> result = doAttributeAggregate("http://mock-sp", "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", port);

    assertEquals(0, result.size());
  }
  @Test
  public void testAttributeAggregateRequiredAttributePresent() throws Exception {
    List<UserAttribute> result = doAttributeAggregate("http://mock-sp", "urn:mace:dir:attribute-def:eduPersonPrincipalName", port);

    assertEquals(1, result.size());

    UserAttribute userAttribute = result.get(0);
    assertEquals("urn:mace:dir:attribute-def:eduPersonOrcid", userAttribute.getName());
    assertEquals(singletonList("urn:x-surfnet:aa1:test"), userAttribute.getValues());
    assertEquals("aa1", userAttribute.getSource());
  }

  @Test
  public void testAttributeAggregateWithoutServiceCheck() throws Exception {
    List<UserAttribute> result = doAttributeAggregateWithoutServiceCheck("urn:mace:dir:attribute-def:eduPersonPrincipalName", port);

    assertEquals(3, result.size());
    result.forEach(userAttribute -> assertFalse(userAttribute.isSkipConsent()));
  }

  @Test
  public void testNoServiceProvider() throws Exception {
    List<UserAttribute> result = doAttributeAggregate("http://unknown-sp", "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", port);

    assertEquals(0, result.size());
  }

}