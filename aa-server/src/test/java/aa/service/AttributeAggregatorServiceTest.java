package aa.service;

import aa.aggregators.TestingAttributeAggregator;
import aa.config.AuthorityConfiguration;
import aa.config.AuthorityResolver;
import aa.model.*;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;

public class AttributeAggregatorServiceTest {

  private AuthorityConfiguration configuration = new AuthorityResolver(
      new DefaultResourceLoader(), "classpath:testAttributeAuthorities.yml").getConfiguration();
  private AttributeAggregatorService subject = new AttributeAggregatorService(configuration.getAuthorities().stream()
      .map(config -> new TestingAttributeAggregator(config, true)).collect(toList()), configuration, -1, 1000 * 60 * 5);

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8889);

  @Test
  public void testAggregate() throws Exception {
    doTestAggregate(0, 6);
    doTestAggregate(250, 3);
    doTestAggregate(1000, 0);
  }

  @Test
  public void testCache() throws Exception {
    Attribute attribute = new Attribute("urn:mace:dir:attribute-def:eduPersonPrincipalName", "aa1");

    AttributeAuthorityConfiguration attributeAuthorityConfiguration = new AttributeAuthorityConfiguration();
    attributeAuthorityConfiguration.setAttributes(singletonList(attribute));
    attributeAuthorityConfiguration.setEndpoint("http://localhost:8889");
    attributeAuthorityConfiguration.setId("aa1");

    AuthorityConfiguration configuration = new AuthorityConfiguration(singletonList(attributeAuthorityConfiguration));
    AttributeAggregatorService subject =
        new AttributeAggregatorService(singletonList(new TestingAttributeAggregator(attributeAuthorityConfiguration, true)), configuration, 1 * 1000, 60 * 1000);

    ServiceProvider sp = new ServiceProvider(singleton(new Aggregation(singleton(attribute))));

    stubAggregationCall(0, "json/attributes/user_attributes_eppn.json");

    List<UserAttribute> input = singletonList(new UserAttribute(attribute.getName(), singletonList("value")));
    List<UserAttribute> result = subject.aggregate(sp, input);
    assertEquals(1, result.size());

    //now throw an Exception, but the cache will hit
    stubFor(get(urlEqualTo("/")).willReturn(aResponse().withStatus(400)));

    result = subject.aggregate(sp, input);
    assertEquals(1, result.size());

    //let the cache expire
    Thread.sleep(1100);

    result = subject.aggregate(sp, input);
    assertEquals(0, result.size());

  }

  private void doTestAggregate(int milliseconds, int expected) throws IOException {
    stubAggregationCall(milliseconds, "json/attributes/user_attributes.json");

    Set<Attribute> attributes = this.configuration.getAuthorities().stream().map(conf -> conf.getAttributes()).flatMap(List::stream).collect(toSet());
    ServiceProvider sp = new ServiceProvider(singleton(new Aggregation(attributes)));

    List<UserAttribute> result = subject.aggregate(sp, singletonList(new UserAttribute("name", singletonList("value"), "source")));
    assertEquals(expected, result.size());
  }

  private void stubAggregationCall(int milliseconds, String path) throws IOException {
    String response = StreamUtils.copyToString(new ClassPathResource(path).getInputStream(), Charset.forName("UTF-8"));

    //the delay will cause the Attribute Aggregators to return empty list
    stubFor(get(urlEqualTo("/")).willReturn(aResponse().withFixedDelay(milliseconds).withStatus(200).withHeader("Content-Type", "application/json").withBody(response)));
  }
}