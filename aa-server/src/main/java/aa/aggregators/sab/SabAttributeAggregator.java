package aa.aggregators.sab;

import aa.aggregators.AbstractAttributeAggregator;
import aa.aggregators.PreemptiveAuthenticationHttpComponentsClientHttpRequestFactory;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import aa.util.StreamUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Stream;

import static aa.util.StreamUtils.singletonOptionalCollector;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class SabAttributeAggregator extends AbstractAttributeAggregator {

  private static final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC);

  private final String template;

  private final SabResponseParser parser = new SabResponseParser();

  public SabAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
    super(attributeAuthorityConfiguration);
    try {
      this.template = IOUtils.toString(new ClassPathResource("sab/request.xml").getInputStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<UserAttribute> aggregate(List<UserAttribute> input) {
    Optional<UserAttribute> userAttribute = input.stream().filter(attr -> attr.getName().equals(NAME_ID)).collect(singletonOptionalCollector());
    if (!userAttribute.isPresent() || userAttribute.get().getValues().isEmpty()) {
      throw new IllegalArgumentException(format("%s requires %s attribute with value", getClass(), NAME_ID));
    }
    String request = request(userAttribute.get().getValues().get(0));
    ResponseEntity<String> response = getRestTemplate().exchange(endpoint(), HttpMethod.POST, new HttpEntity<>(request), String.class);
    try {
      List<String> roles = parser.parse(new StringReader(response.getBody()));
      LOG.debug("Retrieved SAB roles with request: {} and response: {}", request, response);
      return mapResultsToUserAttribute(EDU_PERSON_ENTITLEMENT, roles);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String request(String userId) {
    String issueInstant = dateTimeFormatter.print(System.currentTimeMillis());
    return MessageFormat.format(template, UUID.randomUUID().toString(), issueInstant, userId);
  }

}
