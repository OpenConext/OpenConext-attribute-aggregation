package aa.oidc;

import aa.oauth.AbstractSchacHomeAwareUserAuthenticationConverterTest;
import aa.oauth.ClientCredentialsAuthentication;
import aa.oauth.FederatedUserAuthenticationToken;
import org.junit.Test;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OidcSchacHomeAwareUserAuthenticationConverterTest extends AbstractSchacHomeAwareUserAuthenticationConverterTest {

  private OidcSchacHomeAwareUserAuthenticationConverter subject = new OidcSchacHomeAwareUserAuthenticationConverter();

  @Test
  public void testExtractAuthenticationUserAuthentication() throws Exception {
    FederatedUserAuthenticationToken authentication = (FederatedUserAuthenticationToken) subject.extractAuthentication(readJson("json/oidc/introspect.success.json"));
    assertTrue(authentication.isAuthenticated());
    assertEquals("urn:collab:person:example.com:admin", authentication.getName());
    assertEquals("surfteams.nl", authentication.getSchacHomeOrganization());
  }

  @Test
  public void testExtractAuthenticationClientCredentialsAuthentication() throws Exception {
    ClientCredentialsAuthentication authentication = (ClientCredentialsAuthentication) subject.extractAuthentication(readJson("json/oidc/introspect.client_credentials.json"));
    assertTrue(authentication.isAuthenticated());
    assertEquals("http@//mock-sp", authentication.getName());
  }

  @Test(expected = InvalidClientException.class)
  public void testInvalidClient() {
    subject.extractAuthentication(Collections.emptyMap());
  }

}
