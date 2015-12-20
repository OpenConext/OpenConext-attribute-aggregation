package aa.serviceregistry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ServiceRegistryConfiguration {

  @Bean
  @Profile({"dev", "no-csrf", "acc"})
  public ServiceRegistry classPathResourceServiceRegistry() {
    return new ClassPathResourceServiceRegistry(true);
  }

  @Bean
  @Profile("prod")
  @Autowired
  public ServiceRegistry urlResourceServiceRegistry(
      @Value("${metadata.spRemotePath}") String spRemotePath,
      @Value("${metadata.refresh.minutes}") int period) {
    return new UrlResourceServiceRegistry(spRemotePath, period);
  }

  @Bean
  @Profile({"test"})
  public ServiceRegistry testingServiceRegistry() {
    return new TestingServiceRegistry();
  }

}
