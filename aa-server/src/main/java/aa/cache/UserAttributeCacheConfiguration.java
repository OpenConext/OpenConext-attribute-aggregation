package aa.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserAttributeCacheConfiguration {

    @Bean
    public UserAttributeCache inMemoryUserAttributeCache(@Value("${aggregate.cache.duration.milliseconds}") long cacheDuration) {
        return new SimpleInMemoryUserAttributeCache(cacheDuration, cacheDuration);
    }

}
