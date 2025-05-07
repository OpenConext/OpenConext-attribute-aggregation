package aa.aggregators;

import aa.aggregators.ala.AlaAttributeAggregator;
import aa.aggregators.eduid.EduIdAttributeAggregator;
import aa.aggregators.entitlements.EntitlementsAggregator;
import aa.aggregators.idin.IdinAttributeAggregator;
import aa.aggregators.institution.InstitutionAttributeAggregator;
import aa.aggregators.manage.SurfCrmAttributeAggregator;
import aa.aggregators.orcid.OrcidAttributeAggregator;
import aa.aggregators.pseudo.PseudoEmailAggregator;
import aa.aggregators.rest.RestAttributeAggregator;
import aa.aggregators.sabrest.SabRestAttributeAggregator;
import aa.aggregators.sbs.SBSAttributeAggregator;
import aa.aggregators.test.TestingAttributeAggregator;
import aa.aggregators.voot.VootAttributeAggregator;
import aa.cache.UserAttributeCache;
import aa.config.AuthorityConfiguration;
import aa.config.AuthorityResolver;
import aa.model.AggregatorType;
import aa.model.AttributeAuthorityConfiguration;
import aa.repository.AccountRepository;
import aa.repository.PseudoEmailRepository;
import aa.service.AttributeAggregatorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

@Configuration
public class AttributeAggregatorConfiguration {

    private final String authorizationAccessTokenUrl;
    private final String pseudoMailPostfix;
    private final AuthorityResolver authorityResolver;
    private final UserAttributeCache userAttributeCache;
    private final AccountRepository accountRepository;
    private final PseudoEmailRepository pseudoEmailRepository;
    private final TaskScheduler taskScheduler;
    private final Resource serviceProviderConfigPath;
    private final ObjectMapper objectMapper;

    @Autowired
    public AttributeAggregatorConfiguration(@Value("${authorization_access_token_url}") String authorizationAccessTokenUrl,
                                            @Value("${pseudo.mail_postfix}") String pseudoMailPostfix,
                                            @Value("${institution.service_provider_config_path}") Resource serviceProviderConfigPath,
                                            AuthorityResolver authorityResolver,
                                            UserAttributeCache userAttributeCache,
                                            AccountRepository accountRepository,
                                            PseudoEmailRepository pseudoEmailRepository,
                                            TaskScheduler taskScheduler,
                                            ObjectMapper objectMapper) {
        this.authorizationAccessTokenUrl = authorizationAccessTokenUrl;
        this.pseudoMailPostfix = pseudoMailPostfix;
        this.authorityResolver = authorityResolver;
        this.userAttributeCache = userAttributeCache;
        this.accountRepository = accountRepository;
        this.pseudoEmailRepository = pseudoEmailRepository;
        this.taskScheduler = taskScheduler;
        this.serviceProviderConfigPath = serviceProviderConfigPath;
        this.objectMapper = objectMapper;
    }

    @Bean
    @Profile({"test"})
    public AttributeAggregatorService testingAttributeAggregatorService() {
        return getAttributeAggregatorService(TestingAttributeAggregator::new);
    }

    @Bean
    @Profile({"!test"})
    public AttributeAggregatorService attributeAggregatorService() {
        return getAttributeAggregatorService(this::attributeAggregatorById);
    }

    private AttributeAggregatorService getAttributeAggregatorService(Function<AttributeAuthorityConfiguration, AttributeAggregator> aggregatorFunction) {
        AuthorityConfiguration configuration = authorityResolver.getConfiguration();
        List<AttributeAggregator> attributeAggregators = configuration.getAuthorities().stream()
                .map(aggregatorFunction)
                .filter(Objects::nonNull)
                .collect(toList());
        return new AttributeAggregatorService(attributeAggregators, configuration, userAttributeCache);
    }

    private AttributeAggregator attributeAggregatorById(AttributeAuthorityConfiguration configuration) {
        String id = configuration.getId();
        switch (id) {
            case "voot":
                return new VootAttributeAggregator(configuration, authorizationAccessTokenUrl);
            case "orcid":
                return new OrcidAttributeAggregator(configuration, accountRepository);
            case "idin":
                return new IdinAttributeAggregator(configuration);
            case "pseudo_email":
                return new PseudoEmailAggregator(configuration, pseudoEmailRepository, pseudoMailPostfix);
            case "surfmarket_entitlements":
                return new EntitlementsAggregator(configuration);
            case "sbs":
                return new SBSAttributeAggregator(configuration);
            case "ala":
                return new AlaAttributeAggregator(configuration);
            case "eduid":
                return new EduIdAttributeAggregator(configuration);
            case "manage":
                return new SurfCrmAttributeAggregator(configuration);
            case "sabrest":
                return new SabRestAttributeAggregator(configuration);
            case "institution":
                return new InstitutionAttributeAggregator(configuration, serviceProviderConfigPath, objectMapper);
            default:
                if (id.startsWith("test:")) {
                    return new TestingAttributeAggregator(configuration);
                } else {
                    // Check if there is a type that can be used
                    if (null != configuration.getType()) {
                        if (AggregatorType.rest.equals(configuration.getType())) {
                            RestAttributeAggregator restAggregator = new RestAttributeAggregator(configuration, objectMapper);
                            if (restAggregator.cachingEnabled()) {
                                this.taskScheduler.schedule(restAggregator, new CronTrigger(configuration.getCache().getRefreshCron()));
                            }
                            return restAggregator;
                        }
                    }
                    //We don't want to fail here as it might be that new AA's are already defined but not yet implemented
                    return null;
                }

        }
    }
}
