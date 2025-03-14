package aa.config;

import aa.model.AttributeAuthorityConfiguration;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class AuthorityConfiguration {

    private List<AttributeAuthorityConfiguration> authorities = new ArrayList<>();

    public List<AttributeAuthorityConfiguration> getAuthorities() {
        return authorities;
    }

    public AttributeAuthorityConfiguration getAuthorityById(String authorityId) {
        return authorities.stream().filter(authority -> authority.getId().equals(authorityId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
