package aa.cache;

public class RedisUserAttributeCacheTest extends AbstractUserAttributeCacheTest {

  private UserAttributeCache subject = new UserAttributeCacheConfiguration().redisUserAttributeCache("localhost", 250);

  @Override
  public UserAttributeCache getSubject() {
    return subject;
  }
}