package aa.cache;

import aa.model.UserAttribute;

import java.util.List;
import java.util.Optional;

public interface UserAttributeCache {

  Optional<List<UserAttribute>> get(Optional<String> cacheKey);

  void put(Optional<String> cacheKey, List<UserAttribute> userAttributes);
}
