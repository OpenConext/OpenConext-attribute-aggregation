package aa.cache;

import aa.serviceregistry.ClassPathResourceServiceRegistry;
import aa.serviceregistry.ServiceRegistry;
import aa.serviceregistry.UrlResourceServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.net.MalformedURLException;

@Configuration
public class UserAttributeCacheConfiguration {

  @Bean
  @Profile({"!redis"})
  public UserAttributeCache inMemoryUserAttributeCache(@Value("${aggregate.cache.duration.milliseconds}") long cacheDuration) {
    return new SimpleInMemoryUserAttributeCache(cacheDuration, 1000 * 60 * 5);
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
