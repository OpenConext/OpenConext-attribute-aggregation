package aa.cache;

import aa.model.UserAttribute;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertFalse;

public class SimpleInMemoryUserAttributeCacheTest extends AbstractUserAttributeCacheTest {

  private SimpleInMemoryUserAttributeCache subject = new SimpleInMemoryUserAttributeCache(250, 150);

  @Override
  public UserAttributeCache getSubject() {
    return subject;
  }

  @Test
  public void testCacheInactive() throws IOException {
    UserAttributeCache cache = new SimpleInMemoryUserAttributeCache(-1, 150);
    Optional<String> key = Optional.of("key");
    cache.put(key, singletonList(new UserAttribute("name", singletonList("value"), "source")));
    Optional<List<UserAttribute>> userAttributes = cache.get(key);
    assertFalse(userAttributes.isPresent());

  }
}