package aa.config;

import aa.model.Attribute;
import aa.model.AttributeAuthorityConfiguration;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

public class AuthorityConfiguration {

  private Map<String, AttributeAuthorityConfiguration> authorities = new HashMap<>();

  public AuthorityConfiguration() {
  }

  public AuthorityConfiguration(List<AttributeAuthorityConfiguration> authorities) {
    setAuthorities(authorities);
  }

  public Collection<AttributeAuthorityConfiguration> getAuthorities() {
    return authorities.values();
  }

  public void setAuthorities(List<AttributeAuthorityConfiguration> authorities) {
    this.authorities = authorities.stream().collect(Collectors.toMap(AttributeAuthorityConfiguration::getId, Function.identity()));
  }

  public AttributeAuthorityConfiguration getAuthorityById(String authorityId) {
    AttributeAuthorityConfiguration attributeAuthorityConfiguration = authorities.get(authorityId);
    if (attributeAuthorityConfiguration == null) {
      throw new IllegalArgumentException("AttributeAuthority " + authorityId + " is not configured. Configured are " + authorities);
    }
    return attributeAuthorityConfiguration;
  }

  public List<Attribute> getAttributes(Set<String> names) {
    return getAuthorities().stream().map(AttributeAuthorityConfiguration::getAttributes).flatMap(List::stream)
        .filter(attribute -> names.contains(attribute.getName())).collect(toList());
  }

  @Override
  public String toString() {
    return "AuthorityConfiguration{" +
        "authorities=" + authorities +
        '}';
  }
}
