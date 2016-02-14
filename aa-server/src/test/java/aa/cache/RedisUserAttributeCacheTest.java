package aa.cache;

public class RedisUserAttributeCacheTest extends AbstractUserAttributeCacheTest {

  private UserAttributeCache subject = new UserAttributeCacheConfiguration().redisUserAttributeCache("redis://localhost:6379/0", 250);

  @Override
  public UserAttributeCache getSubject() {
    return subject;
  }
}