package aa.aggregators;

import aa.aggregators.orcid.OrcidAttributeAggregator;
import aa.aggregators.sab.SabAttributeAggregator;
import aa.aggregators.test.TestingAttributeAggregator;
import aa.aggregators.voot.VootAttributeAggregator;
import aa.config.AuthorityConfiguration;
import aa.config.AuthorityResolver;
import aa.model.AttributeAuthorityConfiguration;
import aa.service.AttributeAggregatorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.function.Function;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

@Configuration
public class AttributeAggregatorConfiguration {

  private int expiryIntervalCheckMilliseconds = 1000 * 60 * 5;

  @Value("${authorization.accessToken.url}")
  private String authorizationAccessTokenUrl;

  @Value("${aggregate.cache.duration.milliseconds}")
  private long cacheDuration;

  @Value("${scim.server.environment}")
  private String environment;

  @Autowired
  private AuthorityResolver authorityResolver;

  @Bean
  @Profile({"aa-test"})
  public AttributeAggregatorService testingAttributeAggregatorService() {
    return getAttributeAggregatorService(config -> new TestingAttributeAggregator(config, false));
  }

  @Bean
  @Profile({"!aa-test"})
  public AttributeAggregatorService attributeAggregatorService() {
    return getAttributeAggregatorService(this::attributeAggregatorById);
  }

  private AttributeAggregatorService getAttributeAggregatorService(Function<AttributeAuthorityConfiguration, AttributeAggregator> aggregatorFunction) {
    AuthorityConfiguration configuration = authorityResolver.getConfiguration();
    List<AttributeAggregator> attributeAggregators = configuration.getAuthorities().stream()
        .map(aggregatorFunction).collect(toList());
    return new AttributeAggregatorService(attributeAggregators, configuration, cacheDuration, expiryIntervalCheckMilliseconds);
  }

  private AttributeAggregator attributeAggregatorById(AttributeAuthorityConfiguration configuration) {
    String id = configuration.getId();
    switch (id) {
      case "sab":
        return new SabAttributeAggregator(configuration);
      case "voot":
        return new VootAttributeAggregator(configuration, authorizationAccessTokenUrl);
      case "orcid":
        return new OrcidAttributeAggregator(configuration, environment);
      default:
        if (id.startsWith("test:")) {
          return new TestingAttributeAggregator(configuration, false);
        } else {
          throw new IllegalArgumentException(format("Authority with id %s is unknown", id));
        }

    }
  }
}
