package aa;

import aa.aggregators.AttributeAggregator;
import aa.aggregators.TestingAttributeAggregator;
import aa.config.AuthorityResolver;
import aa.service.AttributeAggregatorService;
import aa.serviceregistry.ClassPathResourceServiceRegistry;
import aa.serviceregistry.ServiceRegistry;
import aa.serviceregistry.TestingServiceRegistry;
import aa.serviceregistry.UrlResourceServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;

import java.util.List;

import static java.util.stream.Collectors.toList;

@SpringBootApplication()
public class Application {

  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }

  @Bean
  @Autowired
  public AttributeAggregatorService attributeAggregatorService(@Value("${aggregate.cache.duration.milliseconds}") long cacheDuration, AuthorityResolver authorityResolver) {
    //TODO add actual AttributeAggregators
    List<AttributeAggregator> attributeAggregators =
        authorityResolver.getConfiguration().getAuthorities().stream().map(
            config -> new TestingAttributeAggregator(config, false)).collect(toList());
    return new AttributeAggregatorService(attributeAggregators, authorityResolver.getConfiguration(), cacheDuration, 1000 * 60 * 5);
  }

}

