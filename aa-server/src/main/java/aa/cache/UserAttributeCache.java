package aa.cache;

import aa.model.UserAttribute;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserAttributeCache {

    Optional<List<UserAttribute>> get(Optional<String> cacheKey) throws IOException;

    void put(Optional<String> cacheKey, List<UserAttribute> userAttributes) throws IOException;
}
