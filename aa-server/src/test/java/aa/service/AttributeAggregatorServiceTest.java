package aa.service;

import aa.aggregators.AttributeAggregator;
import aa.aggregators.test.TestingAttributeAggregator;
import aa.cache.NoopUserAttributeCache;
import aa.cache.SimpleInMemoryUserAttributeCache;
import aa.config.AuthorityConfiguration;
import aa.config.AuthorityResolver;
import aa.model.*;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AttributeAggregatorServiceTest {

  private AuthorityConfiguration configuration = new AuthorityResolver(
      new DefaultResourceLoader(), "classpath:testAttributeAuthorities.yml").getConfiguration();
  private AttributeAggregatorService subject = new AttributeAggregatorService(configuration.getAuthorities().stream()
      .map(config -> new TestingAttributeAggregator(config, true)).collect(toList()), configuration, new NoopUserAttributeCache());

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8889);

  @Test
  public void testAggregateTimeOuts() throws Exception {
    doTestAggregate(0, 6);
    doTestAggregate(250, 3);
    doTestAggregate(1000, 0);
  }

  @Test
  public void testCache() throws Exception {
    Attribute attribute = new Attribute("urn:mace:dir:attribute-def:eduPersonPrincipalName", "aa1");
    attribute.setSkipConsent(true);

    AttributeAuthorityConfiguration attributeAuthorityConfiguration = new AttributeAuthorityConfiguration();
    attributeAuthorityConfiguration.setAttributes(singletonList(attribute));
    attributeAuthorityConfiguration.setEndpoint("http://localhost:8889");
    attributeAuthorityConfiguration.setId("aa1");
    attributeAuthorityConfiguration.setRequiredInputAttributes(singletonList(new RequiredInputAttribute("urn:mace:dir:attribute-def:eduPersonPrincipalName")));

    AuthorityConfiguration configuration = new AuthorityConfiguration(singletonList(attributeAuthorityConfiguration));
    AttributeAggregatorService subject = new AttributeAggregatorService(
        singletonList(new TestingAttributeAggregator(attributeAuthorityConfiguration, true)),
        configuration,
        new SimpleInMemoryUserAttributeCache(1 * 1000, 1000 * 60 * 5));

    ServiceProvider sp = new ServiceProvider(singleton(new Aggregation(singleton(attribute))));

    stubAggregationCall(0, "json/attributes/user_attributes_eppn.json");

    List<UserAttribute> input = singletonList(new UserAttribute(attribute.getName(), singletonList("value")));
    List<UserAttribute> result = subject.aggregate(sp, input);
    assertEquals(1, result.size());
    assertTrue(result.get(0).isSkipConsent());

    //now throw an Exception, but the cache will hit
    stubFor(get(urlEqualTo("/")).willReturn(aResponse().withStatus(400)));

    result = subject.aggregate(sp, input);
    assertEquals(1, result.size());

    //let the cache expire
    Thread.sleep(1100);

    result = subject.aggregate(sp, input);
    assertEquals(0, result.size());
  }

  @Test
  public void testLimitUserAttributes() {
    List<Attribute> attributes = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      Attribute attribute = new Attribute("name" + i, "aa1");
      if (i % 2 == 0) {
        attribute.setSkipConsent(true);
      }
      attributes.add(attribute);
    }
    AttributeAuthorityConfiguration configuration = new AttributeAuthorityConfiguration("aa1", attributes);
    configuration.setEndpoint("http://localhost");
    List<AttributeAuthorityConfiguration> configurations = singletonList(configuration);
    AuthorityConfiguration authorityConfiguration = new AuthorityConfiguration(configurations);
    List<AttributeAggregator> aggregators = singletonList(new TestingAttributeAggregator(configurations.get(0), false));
    AttributeAggregatorService subject = new AttributeAggregatorService(aggregators, authorityConfiguration, new NoopUserAttributeCache());

    Set<Aggregation> aggregations = singleton(new Aggregation(new HashSet<>(attributes.subList(0, 3))));
    ServiceProvider serviceProvider = new ServiceProvider(aggregations);
    List<UserAttribute> userAttributes = subject.aggregate(serviceProvider, singletonList(new UserAttribute("name", singletonList("value"))));
    assertEquals(3, userAttributes.size());
    for (int i = 0; i < 3; i++) {
      UserAttribute userAttribute = userAttributes.get(i);
      assertEquals("name" + i, userAttribute.getName());
      assertEquals("aa1", userAttribute.getSource());
      if (i % 2 == 0) {
        assertEquals(true, userAttribute.isSkipConsent());
      }
    }
  }

  private void doTestAggregate(int milliseconds, int expected) throws IOException {
    stubAggregationCall(milliseconds, "json/attributes/user_attributes.json");

    Set<Attribute> attributes = this.configuration.getAuthorities().stream().map(conf -> conf.getAttributes()).flatMap(List::stream).collect(toSet());
    ServiceProvider sp = new ServiceProvider(singleton(new Aggregation(attributes)));

    //to get any result we must provide the aggregate with the required UserAttribute name. See classpath:testAttributeAuthorities.yml
    List<UserAttribute> result = subject.aggregate(sp, singletonList(new UserAttribute("urn:mace:dir:attribute-def:eduPersonPrincipalName", singletonList("value"), "source")));
    assertEquals(expected, result.size());
  }

  private void stubAggregationCall(int milliseconds, String path) throws IOException {
    String response = IOUtils.toString(new ClassPathResource(path).getInputStream(), Charset.defaultCharset());

    //the delay will cause the Attribute Aggregators to return empty list
    stubFor(get(urlEqualTo("/")).willReturn(aResponse().withFixedDelay(milliseconds).withStatus(200).withHeader("Content-Type", "application/json").withBody(response)));
  }
}