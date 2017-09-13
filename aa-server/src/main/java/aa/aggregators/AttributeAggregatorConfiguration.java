package aa.aggregators;

import aa.aggregators.idin.IdinAttributeAggregator;
import aa.aggregators.orcid.OrcidAttributeAggregator;
import aa.aggregators.pseudo.PseudoEmailAggregator;
import aa.aggregators.sab.SabAttributeAggregator;
import aa.aggregators.test.TestingAttributeAggregator;
import aa.aggregators.voot.VootAttributeAggregator;
import aa.cache.UserAttributeCache;
import aa.config.AuthorityConfiguration;
import aa.config.AuthorityResolver;
import aa.model.AttributeAuthorityConfiguration;
import aa.repository.AccountRepository;
import aa.repository.PseudoEmailRepository;
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

    private String authorizationAccessTokenUrl;
    private String pseudoMailPostfix;
    private AuthorityResolver authorityResolver;
    private UserAttributeCache userAttributeCache;
    private AccountRepository accountRepository;
    private PseudoEmailRepository pseudoEmailRepository;

    @Autowired
    public AttributeAggregatorConfiguration(@Value("${authorization_access_token_url}") String authorizationAccessTokenUrl,
                                            @Value("${pseudo.mail_postfix}") String pseudoMailPostfix,
                                            AuthorityResolver authorityResolver,
                                            UserAttributeCache userAttributeCache,
                                            AccountRepository accountRepository,
                                            PseudoEmailRepository pseudoEmailRepository) {
        this.authorizationAccessTokenUrl = authorizationAccessTokenUrl;
        this.pseudoMailPostfix = pseudoMailPostfix;
        this.authorityResolver = authorityResolver;
        this.userAttributeCache = userAttributeCache;
        this.accountRepository = accountRepository;
        this.pseudoEmailRepository = pseudoEmailRepository;
    }

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
            case "pseudo_email":
                return new PseudoEmailAggregator(configuration, pseudoEmailRepository, pseudoMailPostfix);
            default:
                if (id.startsWith("test:")) {
                    return new TestingAttributeAggregator(configuration);
                } else {
                    throw new IllegalArgumentException(format("Authority with id %s is unknown", id));
                }

        }
    }
}
