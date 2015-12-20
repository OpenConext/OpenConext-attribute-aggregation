package aa.shibboleth;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.authority.AuthorityUtils;

import static aa.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.DISPLAY_NAME_HEADER_NAME;
import static aa.shibboleth.ShibbolethPreAuthenticatedProcessingFilter.UID_HEADER_NAME;
import static org.junit.Assert.*;

public class ShibbolethPreAuthenticatedProcessingFilterTest {

  private ShibbolethPreAuthenticatedProcessingFilter subject = new ShibbolethPreAuthenticatedProcessingFilter(null);

  @Test
  public void testGetPreAuthenticatedPrincipal() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(UID_HEADER_NAME, "urn:collab:person:example.com:admin");
    request.addHeader(DISPLAY_NAME_HEADER_NAME, "John Doe");
    FederatedUser user = (FederatedUser) subject.getPreAuthenticatedPrincipal(request);

    assertEquals("urn:collab:person:example.com:admin", user.getUid());
    assertEquals("John Doe", user.getDisplayName());
    assertTrue(user.getAuthorities().containsAll(AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER")));
  }

  @Test
  public void testGetPreAuthenticatedPrincipalEmpty() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.addHeader(UID_HEADER_NAME, "urn:collab:person:example.com:admin");
    FederatedUser user = (FederatedUser) subject.getPreAuthenticatedPrincipal(request);
    assertNull(user);
  }
}