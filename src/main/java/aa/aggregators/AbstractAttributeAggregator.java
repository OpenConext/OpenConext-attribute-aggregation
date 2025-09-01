package aa.aggregators;

import aa.model.AttributeAuthorityConfiguration;
import aa.model.Cache;
import aa.model.RequiredInputAttribute;
import aa.model.UserAttribute;
import lombok.Getter;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static io.restassured.path.json.JsonPath.config;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.CollectionUtils.isEmpty;

public abstract class AbstractAttributeAggregator implements AttributeAggregator {

    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    @Getter
    private final AttributeAuthorityConfiguration attributeAuthorityConfiguration;

    private final List<String> attributeKeysRequired;

    @Getter
    private RestTemplate restTemplate;

    public AbstractAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        this.attributeAuthorityConfiguration = attributeAuthorityConfiguration;
        this.attributeKeysRequired = attributeAuthorityConfiguration.getRequiredInputAttributes().stream().map
                (RequiredInputAttribute::getName).collect(toList());
        if (StringUtils.hasText(attributeAuthorityConfiguration.getEndpoint())) {
            this.restTemplate = initializeRestTemplate(attributeAuthorityConfiguration);
        }
    }

    protected RestTemplate initializeRestTemplate(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        RestTemplate restTemplate = new RestTemplate(getRequestFactory(attributeAuthorityConfiguration));
        if (StringUtils.hasText(attributeAuthorityConfiguration.getUser())) {
            BasicAuthenticationInterceptor interceptor = new BasicAuthenticationInterceptor(attributeAuthorityConfiguration.getUser(), attributeAuthorityConfiguration.getPassword());
            restTemplate.getInterceptors().add(interceptor);
        }
        return restTemplate;
    }

    @Override
    public List<String> attributeKeysRequired() {
        return attributeKeysRequired;
    }

    @Override
    public Optional<String> cacheKey(List<UserAttribute> input) {
        List<String> requiredKeys = attributeKeysRequired();
        Set<String> values = input.stream()
                .filter(userAttribute -> requiredKeys.contains(userAttribute.getName()))
                .map(UserAttribute::getValues)
                .flatMap(List::stream)
                .collect(toSet());
        //ensure we don't hit the cache accidentally
        return values.isEmpty() ?
                Optional.empty() :
                Optional.of(getAttributeAuthorityId() + "-" + String.join(",", values));

    }

    @Override
    public String getAttributeAuthorityId() {
        return attributeAuthorityConfiguration.getId();
    }

    protected URI endpoint() {
        return endpoint("");
    }

    protected URI endpoint(String postPath) {
        try {
            return new URI(attributeAuthorityConfiguration.getEndpoint() + postPath);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    protected String getUserAttributeSingleValue(List<UserAttribute> input, String nameId) {
        Optional<UserAttribute> userAttribute = input.stream().filter(attr -> attr.getName().equals(nameId))
                .findFirst();
        if (!userAttribute.isPresent() || userAttribute.get().getValues().isEmpty()) {
            throw new IllegalArgumentException(format("%s requires %s attribute with value", getClass(), nameId));
        }
        return userAttribute.get().getValues().get(0);
    }

    protected List<UserAttribute> mapValuesToUserAttribute(String attributeName, List<String> values) {
        if (isEmpty(values)) {
            return emptyList();
        }
        //we need to sort the list to have a consistent consent
        return singletonList(new UserAttribute(attributeName,
                values.stream().sorted().collect(toList()),
                getAttributeAuthorityId()));
    }

    private ClientHttpRequestFactory getRequestFactory(AttributeAuthorityConfiguration
                                                               attributeAuthorityConfiguration) {
        ConnectionConfig connectionConfig = ConnectionConfig
                .custom()
                .setTimeToLive(60, TimeUnit.SECONDS)
                .build();

        PoolingHttpClientConnectionManager connManager =
                PoolingHttpClientConnectionManagerBuilder.create()
                        .setDefaultConnectionConfig(connectionConfig)
                        .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connManager)
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        // Set the connectionRequestTimeout value to 10 seconds
        requestFactory.setConnectionRequestTimeout(10000);
        int timeOut = attributeAuthorityConfiguration.getTimeOut();
        requestFactory.setConnectTimeout(timeOut);
        requestFactory.setReadTimeout(timeOut);
        return requestFactory;
    }

    @Override
    public String toString() {
        return String.format("AttributeAggregator with configuration: %s", this.attributeAuthorityConfiguration);
    }

    @Override
    public List<UserAttribute> filterInvalidResponses(List<UserAttribute> input) {
        String validationRegExp = attributeAuthorityConfiguration.getValidationRegExp();
        final Pattern pattern = Pattern.compile(validationRegExp, Pattern.CASE_INSENSITIVE);
        return input.stream().map(userAttribute -> new UserAttribute(
                        userAttribute.getName(),
                        userAttribute.getValues().stream().filter(value -> filterAttributeValue(userAttribute, value, pattern))
                                .collect(toList()),
                        userAttribute.getSource()))
                .filter(userAttribute -> !CollectionUtils.isEmpty(userAttribute.getValues()))
                .collect(toList());
    }

    protected String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private boolean filterAttributeValue(UserAttribute userAttribute, String value, Pattern pattern) {
        boolean result = pattern.matcher(value).matches();
        if (!result) {
            LOG.warn("Filtered out invalid value {} for userAttribute {} based on pattern {}", value, userAttribute,
                    pattern);
        }
        return result;
    }

    public boolean cachingEnabled() {
        Cache cacheConfig = attributeAuthorityConfiguration.getCache();
        return null != cacheConfig && Boolean.TRUE.equals(cacheConfig.getEnabled());
    }
}
