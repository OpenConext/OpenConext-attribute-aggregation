package aa.aggregators.voot;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.ArpValue;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizationFailureHandler;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.AuthenticatedPrincipalOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toList;
import static org.springframework.security.oauth2.client.web.client.RequestAttributeClientRegistrationIdResolver.clientRegistrationId;

public class VootAttributeAggregator extends AbstractAttributeAggregator {

    public static final String REGISTRATION_ID = "aa";
    private final RestClient restClient;

    public VootAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration,
                                   String authorizationAccessTokenUrl) {
        super(attributeAuthorityConfiguration);
        this.restClient = vootRestClient(attributeAuthorityConfiguration, authorizationAccessTokenUrl);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<UserAttribute> aggregate(List<UserAttribute> input, Map<String, List<ArpValue>> arpAttributes) {
        String userUrn = getUserAttributeSingleValue(input, NAME_ID);
        String url = endpoint() + "/internal/groups/{userUrn}";
        List<Map<String, Object>> objects = restClient
                .get()
                .uri(url, userUrn)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {
        });
        List<String> groups = objects.stream().map(entry -> (String) entry.get("id")).collect(toList());

        LOG.debug("Retrieved VOOT groups with request: {} and response: {}", url, groups);

        return mapValuesToUserAttribute(IS_MEMBER_OF, groups);
    }

    private RestClient vootRestClient(AttributeAuthorityConfiguration configuration,
                                      String authorizationAccessTokenUrl) {
        ClientRegistration clientRegistration = clientRegistration(configuration, authorizationAccessTokenUrl);
        InMemoryClientRegistrationRepository clientRegistrationRepository = new InMemoryClientRegistrationRepository(clientRegistration);
        AuthenticatedPrincipalOAuth2AuthorizedClientRepository authorizedClientRepository = new AuthenticatedPrincipalOAuth2AuthorizedClientRepository(
                new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)
        );

        OAuth2AuthorizedClientManager authorizedClientManager = new DefaultOAuth2AuthorizedClientManager(
                clientRegistrationRepository,
                authorizedClientRepository
        );
        //TODO find out how and if access_tokens are cached, maybe 
        AuthorizedClientServiceOAuth2AuthorizedClientManager clientManager =
                new AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository));
//        OAuth2ClientHttpRequestInterceptor requestInterceptor = new OAuth2ClientHttpRequestInterceptor(authorizedClientManager);
        OAuth2ClientHttpRequestInterceptor requestInterceptor = new OAuth2ClientHttpRequestInterceptor(clientManager);
        OAuth2AuthorizationFailureHandler authorizationFailureHandler = OAuth2ClientHttpRequestInterceptor.authorizationFailureHandler(authorizedClientRepository);
        requestInterceptor.setAuthorizationFailureHandler(authorizationFailureHandler);
        requestInterceptor.setClientRegistrationIdResolver(new DefaulltClientRegistrationIdResolver());
        RestClient.Builder builder = RestClient.builder();
        return builder.requestInterceptor(requestInterceptor).build();
    }

    private ClientRegistration clientRegistration(AttributeAuthorityConfiguration configuration,
                                                  String authorizationAccessTokenUrl) {
        return ClientRegistration
                .withRegistrationId(REGISTRATION_ID)
                .tokenUri(authorizationAccessTokenUrl)
                .clientId(configuration.getUser())
                .clientSecret(configuration.getPassword())
                .scope("groups")
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .build();
    }

}
