package aa.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
public class UserAttributeCacheConfiguration {

  @Bean
  @Profile({"!redis"})
  public UserAttributeCache inMemoryUserAttributeCache(@Value("${aggregate.cache.duration.milliseconds}") long cacheDuration) {
    return new SimpleInMemoryUserAttributeCache(cacheDuration, cacheDuration);
  }

  @Bean
  @Profile({"redis"})
  @Autowired
  @Primary
  public UserAttributeCache redisUserAttributeCache(@Value("${redis.url}") String redisUrl,
                                                    @Value("${aggregate.cache.duration.milliseconds}") long cacheDuration) {
    return new RedisUserAttributeCache(redisUrl, cacheDuration);
  }

}
