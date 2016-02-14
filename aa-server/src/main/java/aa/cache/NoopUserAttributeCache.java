package aa.cache;

import aa.model.UserAttribute;

import java.util.List;
import java.util.Optional;

public class NoopUserAttributeCache implements UserAttributeCache {

  @Override
  public Optional<List<UserAttribute>> get(Optional<String> cacheKey) {
    return Optional.empty();
  }

  @Override
  public void put(Optional<String> cacheKey, List<UserAttribute> userAttributes) {

  }
}
