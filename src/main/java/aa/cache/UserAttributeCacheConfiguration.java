package aa.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserAttributeCacheConfiguration {

    @Bean
    public UserAttributeCache inMemoryUserAttributeCache(@Value("${aggregate_cache_duration_milliseconds}") long cacheDuration) {
        return cacheDuration < 0 ? new NoopUserAttributeCache() : new SimpleInMemoryUserAttributeCache(cacheDuration, cacheDuration);
    }

}
