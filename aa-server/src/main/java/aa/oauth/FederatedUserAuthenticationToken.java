package aa.oauth;

import aa.model.UserAttribute;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static aa.aggregators.AttributeAggregator.EDU_PERSON_PRINCIPAL_NAME;
import static aa.aggregators.AttributeAggregator.NAME_ID;
import static aa.aggregators.AttributeAggregator.SCHAC_HOME;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

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

  public List<UserAttribute> getUserAttributes() {
    //need ArrayList otherwise we can't add anything
    List<UserAttribute> input = new ArrayList<>(asList(
        new UserAttribute(NAME_ID, singletonList(getName())),
        new UserAttribute(SCHAC_HOME, singletonList(schacHomeOrganization))
    ));
    if (StringUtils.hasText(eduPersonPrincipalName)) {
      input.add(new UserAttribute(EDU_PERSON_PRINCIPAL_NAME, singletonList(eduPersonPrincipalName)));
    }
    return input;
  }

}
