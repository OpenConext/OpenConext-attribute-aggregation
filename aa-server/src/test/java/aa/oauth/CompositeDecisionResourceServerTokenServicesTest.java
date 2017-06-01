package aa.oauth;

import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompositeDecisionResourceServerTokenServicesTest {

    private static final String principal = "principal";
    private static final String value = "value";

    private CompositeDecisionResourceServerTokenServices tokenServices;
    private String accessToken = "df404eef-1efb-4c91-bc65-f977bf945abe";

    @Before
    public void before() {
        DecisionResourceServerTokenServices authz = mock(DecisionResourceServerTokenServices.class);
        when(authz.canHandle(accessToken)).thenReturn(true);
        when(authz.loadAuthentication(accessToken)).thenReturn(new OAuth2Authentication(null, new TestingAuthenticationToken(principal, "N/A")));
        when(authz.readAccessToken(accessToken)).thenReturn(new DefaultOAuth2AccessToken(value));

        DecisionResourceServerTokenServices oidc = mock(DecisionResourceServerTokenServices.class);
        when(oidc.canHandle(accessToken)).thenReturn(false);

        tokenServices = new CompositeDecisionResourceServerTokenServices(Arrays.asList(authz, oidc));
    }

    @Test
    public void testCanHandle() throws Exception {
        assertTrue(tokenServices.canHandle(accessToken));
    }

    @Test
    public void testLoadAuthentication() throws Exception {
        assertEquals(principal, tokenServices.loadAuthentication(accessToken).getPrincipal());
    }

    @Test
    public void testReadAccessToken() throws Exception {
        assertEquals(value, tokenServices.readAccessToken(accessToken).getValue());
    }

    @Test
    public void testNoHandlers() {
        tokenServices = new CompositeDecisionResourceServerTokenServices(Collections.EMPTY_LIST);
        assertFalse(tokenServices.canHandle("N/A"));
    }

    @Test(expected = InvalidTokenException.class)
    public void testNoHandlersLoadAuthentication() {
        tokenServices = new CompositeDecisionResourceServerTokenServices(Collections.EMPTY_LIST);
        tokenServices.loadAuthentication("N/A");
    }

    @Test(expected = InvalidTokenException.class)
    public void testNoHandlersReadAcessToken() {
        tokenServices = new CompositeDecisionResourceServerTokenServices(Collections.EMPTY_LIST);
        tokenServices.readAccessToken("N/A");
    }

}
