package aa.shibboleth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class FederatedUser extends User {

  public final String uid;
  public final String displayName;

  public FederatedUser(String uid, String displayName, List<GrantedAuthority> authorities) {
    super(uid, "N/A", authorities);
    this.uid = uid;
    this.displayName = displayName;
  }

  public String getUid() {
    return uid;
  }

  public String getDisplayName() {
    return displayName;
  }
}
