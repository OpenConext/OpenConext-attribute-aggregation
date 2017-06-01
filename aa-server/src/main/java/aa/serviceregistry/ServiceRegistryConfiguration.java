package aa.serviceregistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
public class ServiceRegistryConfiguration {

    @Bean
    @Profile({"dev", "no-csrf", "aa-test"})
    public ServiceRegistry classPathResourceServiceRegistry() throws IOException {
        return new ClassPathResourceServiceRegistry(true);
    }

    @Bean
    @Profile({"test", "acc", "prod"})
    @Autowired
    @Primary
    public ServiceRegistry urlResourceServiceRegistry(
        @Value("${metadata.username}") String username,
        @Value("${metadata.password}") String password,
        @Value("${metadata.spRemotePath}") String spRemotePath,
        @Value("${metadata.refresh.minutes}") int period) throws IOException {
        return new UrlResourceServiceRegistry(username, password, spRemotePath, period);
    }

}
