package aa.aggregators;

import aa.aggregators.idin.IdinAttributeAggregator;
import aa.aggregators.orcid.OrcidAttributeAggregator;
import aa.aggregators.sab.SabAttributeAggregator;
import aa.aggregators.test.TestingAttributeAggregator;
import aa.aggregators.voot.VootAttributeAggregator;
import aa.cache.UserAttributeCache;
import aa.config.AuthorityConfiguration;
import aa.config.AuthorityResolver;
import aa.model.AttributeAuthorityConfiguration;
import aa.repository.AccountRepository;
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

    @Value("${authorization_access_token_url}")
    private String authorizationAccessTokenUrl;

    @Value("${scim_server_environment}")
    private String environment;

    @Autowired
    private AuthorityResolver authorityResolver;

    @Autowired
    private UserAttributeCache userAttributeCache;

    @Autowired
    private AccountRepository accountRepository;


    @Bean
    @Profile({"aa-test"})
    public AttributeAggregatorService testingAttributeAggregatorService() {
        return getAttributeAggregatorService(config -> new TestingAttributeAggregator(config));
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
        return new AttributeAggregatorService(attributeAggregators, configuration, userAttributeCache);
    }

    private AttributeAggregator attributeAggregatorById(AttributeAuthorityConfiguration configuration) {
        String id = configuration.getId();
        switch (id) {
            case "sab":
                return new SabAttributeAggregator(configuration);
            case "voot":
                return new VootAttributeAggregator(configuration, authorizationAccessTokenUrl);
            case "orcid":
                return new OrcidAttributeAggregator(configuration, accountRepository);
            case "idin":
                return new IdinAttributeAggregator(configuration);
            default:
                if (id.startsWith("test:")) {
                    return new TestingAttributeAggregator(configuration);
                } else {
                    throw new IllegalArgumentException(format("Authority with id %s is unknown", id));
                }

        }
    }
}
