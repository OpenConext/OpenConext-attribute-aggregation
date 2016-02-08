package aa.serviceregistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.net.MalformedURLException;

@Configuration
public class ServiceRegistryConfiguration {

  @Bean
  @Profile({"dev", "no-csrf"})
  public ServiceRegistry classPathResourceServiceRegistry() {
    return new ClassPathResourceServiceRegistry(true);
  }

  @Bean
  @Profile({"test", "acc", "prod"})
  @Autowired
  public ServiceRegistry urlResourceServiceRegistry(
      @Value("${metadata.username}") String username,
      @Value("${metadata.password}") String password,
      @Value("${metadata.spRemotePath}") String spRemotePath,
      @Value("${metadata.refresh.minutes}") int period) throws MalformedURLException {
    return new UrlResourceServiceRegistry(username, password, spRemotePath, period);
  }

}
