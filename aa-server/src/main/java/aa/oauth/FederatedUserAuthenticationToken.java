package aa.oauth;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class FederatedUserAuthenticationToken extends UsernamePasswordAuthenticationToken {

  private final String schacHomeOrganization;
  private final String eduPersonPrincipalName;

  public FederatedUserAuthenticationToken(String eduPersonPrincipalName, String schacHomeOrganization, Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
    super(principal, credentials, authorities);
    this.eduPersonPrincipalName = eduPersonPrincipalName;
    this.schacHomeOrganization = schacHomeOrganization;
  }

  public String getEduPersonPrincipalName() {
    return eduPersonPrincipalName;
  }

  public String getSchacHomeOrganization() {
    return schacHomeOrganization;
  }

}
