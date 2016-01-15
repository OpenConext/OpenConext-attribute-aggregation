package aa.aggregators;

import aa.aggregators.sab.SabAttributeAggregator;
import aa.aggregators.test.TestingAttributeAggregator;
import aa.config.AuthorityResolver;
import aa.model.AttributeAuthorityConfiguration;
import aa.service.AttributeAggregatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Configuration
public class AttributeAggregatorConfiguration {

  private int expiryIntervalCheckMilliseconds = 1000 * 60 * 5;


  @Bean
  @Profile({"aa-test"})
  @Autowired
  public AttributeAggregatorService testingAttributeAggregatorService(@Value("${aggregate.cache.duration.milliseconds}") long cacheDuration, AuthorityResolver authorityResolver) {
    List<AttributeAggregator> attributeAggregators = authorityResolver.getConfiguration().getAuthorities().stream()
        .map(config -> new TestingAttributeAggregator(config, false)).collect(toList());
    return new AttributeAggregatorService(attributeAggregators, authorityResolver.getConfiguration(), cacheDuration, expiryIntervalCheckMilliseconds);
  }

  @Bean
  @Profile({"!aa-test"})
  @Autowired
  public AttributeAggregatorService attributeAggregatorService(@Value("${aggregate.cache.duration.milliseconds}") long cacheDuration, AuthorityResolver authorityResolver) {
    List<AttributeAggregator> attributeAggregators = authorityResolver.getConfiguration().getAuthorities().stream()
        .map(this::attributeAggregatorById).collect(toList());
    return new AttributeAggregatorService(attributeAggregators, authorityResolver.getConfiguration(), cacheDuration, expiryIntervalCheckMilliseconds);
  }


  private AttributeAggregator attributeAggregatorById(AttributeAuthorityConfiguration configuration) {
    switch (configuration.getId()) {
      case "sab":
        return new SabAttributeAggregator(configuration);
      default:
        throw new IllegalArgumentException(String.format("Authority with id %s in unknown", configuration.getId()));
    }
  }
}
