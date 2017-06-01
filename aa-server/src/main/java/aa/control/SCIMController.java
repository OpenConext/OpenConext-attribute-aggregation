package aa.control;

import aa.config.AuthorityConfiguration;
import aa.config.AuthorityResolver;
import aa.model.Aggregation;
import aa.model.Attribute;
import aa.model.InvalidAuthenticationException;
import aa.model.MetaInformation;
import aa.model.ResourceType;
import aa.model.Schema;
import aa.model.SchemaNotFoundException;
import aa.model.ServiceProvider;
import aa.model.UserAttribute;
import aa.oauth.ClientCredentialsAuthentication;
import aa.oauth.FederatedUserAuthenticationToken;
import aa.repository.ServiceProviderRepository;
import aa.service.AttributeAggregatorService;
import aa.service.ServiceProviderTranslationService;
import aa.serviceregistry.ServiceRegistry;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static aa.aggregators.AttributeAggregator.EDU_PERSON_PRINCIPAL_NAME;
import static aa.aggregators.AttributeAggregator.NAME_ID;
import static aa.aggregators.AttributeAggregator.SCHAC_HOME;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class SCIMController {

    private final static Logger LOG = LoggerFactory.getLogger(SCIMController.class);

    private final String serviceProviderConfigJson;

    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceProviderTranslationService serviceProviderTranslationService;
    private final AttributeAggregatorService attributeAggregatorService;
    private final AuthorityConfiguration authorityConfiguration;
    private final ServiceRegistry serviceRegistry;

    private final String dateTime;
    private final String resourcelocation;
    private final String userlocationPrefix;

    @Autowired
    public SCIMController(@Value("${scim.server.environment}") String env,
                          ServiceProviderRepository serviceProviderRepository,
                          ServiceProviderTranslationService serviceProviderTranslationService,
                          AttributeAggregatorService attributeAggregatorService,
                          AuthorityResolver authorityResolver,
                          ServiceRegistry serviceRegistry) throws IOException {

        this.serviceProviderRepository = serviceProviderRepository;
        this.serviceProviderTranslationService = serviceProviderTranslationService;
        this.attributeAggregatorService = attributeAggregatorService;
        this.authorityConfiguration = authorityResolver.getConfiguration();
        this.serviceRegistry = serviceRegistry;

        this.serviceProviderConfigJson = String.format(IOUtils.toString(new ClassPathResource("scim/ServiceProviderConfig.template.json").getInputStream(), Charset.defaultCharset()), env);

        this.dateTime = RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
        this.resourcelocation = String.format("https://aa.%s.nl/v2/ResourceTypes/Me", env);
        this.userlocationPrefix = String.format("https://aa.%s.nl/v2/Users/", env);
    }

    @RequestMapping(method = RequestMethod.GET, value = {"/v2/ServiceProviderConfig", "/internal/v2/ServiceProviderConfig"})
    public String serviceProviderConfiguration() {
        return serviceProviderConfigJson;
    }


    @RequestMapping(method = RequestMethod.GET, value = "/internal/v2/ResourceType")
    public ResourceType internalResourceType(@RequestParam("serviceProviderEntityId") String serviceProviderEntityId) {
        OAuth2Authentication authentication = buildOAuth2Authentication(serviceProviderEntityId);
        return resourceType(authentication);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/v2/ResourceType")
    public ResourceType resourceType(OAuth2Authentication authentication) {
        assertClientCredentials(authentication);
        String clientId = authentication.getOAuth2Request().getClientId();
        String serviceProviderEntityId = serviceProviderTranslationService.translateClientId(clientId);

        Optional<ServiceProvider> serviceProvider = serviceRegistry.serviceProviderByEntityId(serviceProviderEntityId);
        if (!serviceProvider.isPresent()) {
            throw new SchemaNotFoundException("Service Provider " + serviceProviderEntityId + " is unknown");
        }
        MetaInformation metaInformation = new MetaInformation(this.dateTime, this.dateTime, this.resourcelocation, "ResourceType");
        return new ResourceType(serviceProviderEntityId, metaInformation);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/internal/v2/Schema")
    public Schema internalSchema(@RequestParam("serviceProviderEntityId") String serviceProviderEntityId) {
        OAuth2Authentication authentication = buildOAuth2Authentication(serviceProviderEntityId);
        return schema(authentication);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/v2/Schema")
    public Schema schema(OAuth2Authentication authentication) {
        assertClientCredentials(authentication);
        String clientId = authentication.getOAuth2Request().getClientId();
        String serviceProviderEntityId = serviceProviderTranslationService.translateClientId(clientId);

        Optional<ServiceProvider> sp = serviceProviderRepository.findByEntityId(serviceProviderEntityId);
        if (!sp.isPresent()) {
            throw new SchemaNotFoundException("ServiceProvider " + serviceProviderEntityId + " has no attribute aggregations configured");
        }
        //the attribute pointers, note that no metadata is stored in the database
        List<Attribute> configuredAttributes = sp.get().getAggregations().stream().map(Aggregation::getAttributes).flatMap(Set::stream).collect(toList());

        //attributes from the authorityConfiguration with metadata
        List<Attribute> attributes = authorityConfiguration.getAttributes(configuredAttributes.stream().map(Attribute::getName).collect(toSet()));

        //we don't want to include the attributeAuthorityId, so we need copy with erased attributeAuthorityId
        List<Attribute> clonedAttributes = attributes.stream().map(this::eraseAuthorityId).collect(toList());

        Schema schema = new Schema(
            "Attribute schema for " + serviceProviderEntityId,
            "urn:scim:schemas:extension:surf:" + serviceProviderEntityId,
            serviceProviderEntityId,
            clonedAttributes);

        LOG.debug("Returning schema {} for {}", schema, serviceProviderEntityId);

        return schema;
    }

    @RequestMapping(method = RequestMethod.POST, value = "/internal/v2/Me")
    public Map<String, Object> internalMe(@RequestParam("serviceProviderEntityId") String serviceProviderEntityId, @RequestBody Map<String, String> inputParameters) {
        String clientId = serviceProviderTranslationService.translateServiceProviderEntityId(serviceProviderEntityId);
        OAuth2Request oauth2Request = buildOAuth2Request(clientId);

        String principal = inputParameters.get(NAME_ID);
        String eduPersonPrincipalName = inputParameters.get(EDU_PERSON_PRINCIPAL_NAME);
        String schacHomeOrganization = inputParameters.get(SCHAC_HOME);

        FederatedUserAuthenticationToken userAuthentication = new FederatedUserAuthenticationToken(eduPersonPrincipalName, schacHomeOrganization, principal, "N/A", AuthorityUtils.createAuthorityList("ROLE_USER"));

        OAuth2Authentication authentication = new OAuth2Authentication(oauth2Request, userAuthentication);
        return me(authentication);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/v2/Me")
    public Map<String, Object> me(OAuth2Authentication authentication) {
        assertAuthorizationCode(authentication);
        String clientId = authentication.getOAuth2Request().getClientId();
        String serviceProviderEntityId = serviceProviderTranslationService.translateClientId(clientId);

        Optional<ServiceProvider> sp = serviceProviderRepository.findByEntityId(serviceProviderEntityId);
        if (!sp.isPresent()) {
            throw new SchemaNotFoundException("ServiceProvider " + serviceProviderEntityId + " has no attribute aggregations configured");
        }
        FederatedUserAuthenticationToken userAuthentication = (FederatedUserAuthenticationToken) authentication.getUserAuthentication();
        List<UserAttribute> input = userAuthentication.getUserAttributes();
        List<UserAttribute> userAttributes = attributeAggregatorService.aggregate(sp.get(), input);

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("schemas", singletonList("urn:scim:schemas:extension:surf:" + serviceProviderEntityId));
        String id = UUID.randomUUID().toString();
        result.put("id", id);

        Map<String, List<UserAttribute>> grouped = userAttributes.stream().collect(groupingBy(UserAttribute::getName));

        //now populate the result Map with key UserAttribute::getName and Value the flattened List of values
        grouped.entrySet().forEach(entry -> result.put(entry.getKey(), entry.getValue().stream()
            .map(UserAttribute::getValues).flatMap(List::stream).collect(toList())));

        result.put("meta", new MetaInformation(this.dateTime, this.dateTime, this.userlocationPrefix.concat(id), "User"));

        LOG.debug("Returning Me {} for {}", result, input);

        return result;

    }

    private OAuth2Request buildOAuth2Request(String clientId) {
        return new OAuth2Request(Collections.emptyMap(), clientId,
            SecurityContextHolder.getContext().getAuthentication().getAuthorities(), true, Collections.emptySet(),
            Collections.emptySet(), null, Collections.emptySet(),
            Collections.emptyMap());
    }

    private OAuth2Authentication buildOAuth2Authentication(@RequestParam("serviceProviderEntityId") String serviceProviderEntityId) {
        String clientId = serviceProviderTranslationService.translateServiceProviderEntityId(serviceProviderEntityId);
        OAuth2Request oauth2Request = buildOAuth2Request(clientId);
        return new OAuth2Authentication(oauth2Request,
            new ClientCredentialsAuthentication(clientId, emptyList()));
    }

    private void assertClientCredentials(OAuth2Authentication authentication) {
        assertCorrectAuthentication(authentication, ClientCredentialsAuthentication.class);
    }

    private void assertAuthorizationCode(OAuth2Authentication authentication) {
        assertCorrectAuthentication(authentication, FederatedUserAuthenticationToken.class);
    }

    private void assertCorrectAuthentication(OAuth2Authentication authentication, Class expectedAuthentication) {
        Authentication userAuthentication = authentication.getUserAuthentication();
        if (!userAuthentication.getClass().isAssignableFrom(expectedAuthentication)) {
            throw new InvalidAuthenticationException(
                String.format("Only accessible with %s code authentication. Actual authentication %s",
                    expectedAuthentication.getName(),
                    userAuthentication));

        }
    }

    private Attribute eraseAuthorityId(Attribute attribute) {
        Attribute clone = attribute.clone();
        clone.setAttributeAuthorityId(null);
        return clone;
    }
}
