package aa.aggregators;

import aa.model.AttributeAuthorityConfiguration;
import aa.model.RequiredInputAttribute;
import aa.model.UserAttribute;
import aa.util.StreamUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static aa.util.StreamUtils.singletonOptionalCollector;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.util.CollectionUtils.isEmpty;

public abstract class AbstractAttributeAggregator implements AttributeAggregator {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  private final AttributeAuthorityConfiguration attributeAuthorityConfiguration;

  private final RestTemplate restTemplate;
  private final List<String> attributeKeysRequired;

  public AbstractAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
    this.attributeAuthorityConfiguration = attributeAuthorityConfiguration;
    this.attributeKeysRequired = attributeAuthorityConfiguration.getRequiredInputAttributes().stream().map(RequiredInputAttribute::getName).collect(toList());
    try {
      this.restTemplate = new RestTemplate(getRequestFactory());
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
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

  public AttributeAuthorityConfiguration getAttributeAuthorityConfiguration() {
    return attributeAuthorityConfiguration;
  }

  protected RestTemplate getRestTemplate() {
    return restTemplate;
  }

  protected URI endpoint() {
    try {
      return new URI(attributeAuthorityConfiguration.getEndpoint());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  protected String getUserAttributeSingleValue(List<UserAttribute> input, String nameId) {
    Optional<UserAttribute> userAttribute = input.stream().filter(attr -> attr.getName().equals(nameId)).collect(singletonOptionalCollector());
    if (!userAttribute.isPresent() || userAttribute.get().getValues().isEmpty()) {
      throw new IllegalArgumentException(format("%s requires %s attribute with value", getClass(), nameId));
    }
    return userAttribute.get().getValues().get(0);
  }

  protected List<UserAttribute> mapValuesToUserAttribute(String attributeName, List<String> values) {
    if (isEmpty(values)) {
      return emptyList();
    }
    return Collections.singletonList(new UserAttribute(attributeName, values, getAttributeAuthorityId()));
  }

  protected ClientHttpRequestFactory getRequestFactory() throws MalformedURLException {
    HttpClientBuilder httpClientBuilder = HttpClientBuilder.create().evictExpiredConnections().evictIdleConnections(10l, TimeUnit.SECONDS);
    if (StringUtils.hasText(attributeAuthorityConfiguration.getUser())) {
      BasicCredentialsProvider basicCredentialsProvider = new BasicCredentialsProvider();
      basicCredentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(attributeAuthorityConfiguration.getUser(), attributeAuthorityConfiguration.getPassword()));
      httpClientBuilder.setDefaultCredentialsProvider(basicCredentialsProvider);
    }
    int timeOut = attributeAuthorityConfiguration.getTimeOut();
    httpClientBuilder.setDefaultRequestConfig(RequestConfig.custom().setConnectionRequestTimeout(timeOut).setConnectTimeout(timeOut).setSocketTimeout(timeOut).build());

    CloseableHttpClient httpClient = httpClientBuilder.build();
    return new PreemptiveAuthenticationHttpComponentsClientHttpRequestFactory(httpClient, attributeAuthorityConfiguration.getEndpoint());
  }

  @Override
  public String toString() {
    return String.format("AttributeAggregator with configuration: %s", this.attributeAuthorityConfiguration);
  }

}
