package aa.cache;

public class SimpleInMemoryUserAttributeCacheTest extends AbstractUserAttributeCacheTest {

  private SimpleInMemoryUserAttributeCache subject = new SimpleInMemoryUserAttributeCache(250, 150);

  @Override
  public UserAttributeCache getSubject() {
    return subject;
  }
}