package aa.shibboleth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public class FederatedUser extends User {

    public final String uid;
    public final String displayName;
    public final String schacHomeOrganization;
    private String redirectURI;

    public FederatedUser(String uid, String displayName, String schacHomeOrganization, List<GrantedAuthority> authorities) {
        super(uid, "N/A", authorities);
        this.uid = uid;
        this.displayName = displayName;
        this.schacHomeOrganization = schacHomeOrganization;
    }

    public String getRedirectURI() {
        return redirectURI;
    }

    public void setRedirectURI(String redirectURI) {
        this.redirectURI = redirectURI;
    }
}
