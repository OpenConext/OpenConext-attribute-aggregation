package aa.shibboleth;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.authority.AuthorityUtils;

import static aa.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.DISPLAY_NAME_HEADER_NAME;
import static aa.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.NAME_ID_HEADER_NAME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ShibbolethPreAuthenticatedProcessingFilterTest {

    private ShibbolethPreAuthenticatedProcessingFilter subject = new ShibbolethPreAuthenticatedProcessingFilter(null);
    private MockHttpServletRequest request;

    @Before
    public void before() {
        this.request = new MockHttpServletRequest();
    }

    @Test
    public void testGetPreAuthenticatedPrincipal() throws Exception {
        request.addHeader(NAME_ID_HEADER_NAME, "urn:collab:person:example.com:admin");
        request.addHeader(DISPLAY_NAME_HEADER_NAME, "John Doe");
        FederatedUser user = (FederatedUser) subject.getPreAuthenticatedPrincipal(request);

        assertEquals("urn:collab:person:example.com:admin", user.uid);
        assertEquals("John Doe", user.displayName);
        assertTrue(user.getAuthorities().containsAll(AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER")));
    }

    @Test
    public void testGetPreAuthenticatedPrincipalEmpty() throws Exception {
        request.addHeader(NAME_ID_HEADER_NAME, "");
        assertNull(subject.getPreAuthenticatedPrincipal(request));
    }

    @Test
    public void testGetPreAuthenticatedUdEmpty() throws Exception {
        request.addHeader(DISPLAY_NAME_HEADER_NAME, "John Doe");
        assertNull(subject.getPreAuthenticatedPrincipal(request));
    }
}