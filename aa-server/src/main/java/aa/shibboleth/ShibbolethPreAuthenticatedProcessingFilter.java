package aa.shibboleth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

import static org.springframework.security.core.authority.AuthorityUtils.createAuthorityList;

public class ShibbolethPreAuthenticatedProcessingFilter extends AbstractPreAuthenticatedProcessingFilter {

  public static final String UID_HEADER_NAME = "uid";
  public static final String DISPLAY_NAME_HEADER_NAME = "displayname";

  public ShibbolethPreAuthenticatedProcessingFilter(AuthenticationManager authenticationManager) {
    super();
    setAuthenticationManager(authenticationManager);
  }

  @Override
  protected Object getPreAuthenticatedPrincipal(final HttpServletRequest request) {
    String uid = request.getHeader(UID_HEADER_NAME);
    String displayName = request.getHeader(DISPLAY_NAME_HEADER_NAME);
    if (StringUtils.isEmpty(uid) || StringUtils.isEmpty(displayName)) {
      //this is the contract. See AbstractPreAuthenticatedProcessingFilter#doAuthenticate
      return null;
    }
    return new FederatedUser(uid, displayName, createAuthorityList("ROLE_USER", "ROLE_ADMIN"));
  }

  @Override
  protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    return "N/A";
  }

}
