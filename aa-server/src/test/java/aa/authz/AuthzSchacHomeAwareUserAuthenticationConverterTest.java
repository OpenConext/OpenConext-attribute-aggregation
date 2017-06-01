package aa.authz;

import aa.oauth.AbstractSchacHomeAwareUserAuthenticationConverterTest;
import aa.oauth.ClientCredentialsAuthentication;
import aa.oauth.FederatedUserAuthenticationToken;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AuthzSchacHomeAwareUserAuthenticationConverterTest extends AbstractSchacHomeAwareUserAuthenticationConverterTest {

    private AuthzSchacHomeAwareUserAuthenticationConverter subject = new AuthzSchacHomeAwareUserAuthenticationConverter();

    @Test
    public void testExtractAuthenticationUserAuthentication() throws Exception {
        FederatedUserAuthenticationToken authentication = (FederatedUserAuthenticationToken) subject.extractAuthentication(readJson("json/authz/check_token.success.json"));
        assertTrue(authentication.isAuthenticated());
        assertEquals("urn:collab:person:surfteams.nl:admin", authentication.getName());
        assertEquals("surfteams.nl", authentication.getSchacHomeOrganization());
    }

    @Test
    public void testExtractAuthenticationClientCredentialsAuthentication() throws Exception {
        ClientCredentialsAuthentication authentication = (ClientCredentialsAuthentication) subject.extractAuthentication(readJson("json/authz/check_token.client_credentials.json"));
        assertTrue(authentication.isAuthenticated());
        assertEquals("http@//mock-sp", authentication.getName());
    }

}
