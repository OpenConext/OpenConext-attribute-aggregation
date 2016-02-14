package aa.cache;

import aa.model.UserAttribute;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public abstract class AbstractUserAttributeCacheTest {

  private List<UserAttribute> userAttributes = singletonList(new UserAttribute("name", asList("value1", "value2")));
  private Optional<String> key = Optional.of("key");

  public abstract UserAttributeCache getSubject();

  @Test
  public void testExpireCache() throws Exception {
    getSubject().put(key, userAttributes);
    Thread.sleep(500);
    Optional<List<UserAttribute>> result = getSubject().get(key);
    assertFalse(result.isPresent());
  }

  @Test
  public void testCacheHit() throws Exception {
    getSubject().put(key, userAttributes);
    Optional<List<UserAttribute>> result = getSubject().get(key);
    assertEquals(userAttributes, result.get());
  }

  @Test
  public void testCacheMiss() throws Exception {
    getSubject().put(Optional.empty(), userAttributes);
    Optional<List<UserAttribute>> result = getSubject().get(Optional.empty());
    assertFalse(result.isPresent());
  }

}