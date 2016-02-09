package aa.aggregators;

import aa.config.AuthorityResolver;
import aa.service.AttributeAggregatorService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class AttributeAggregatorConfigurationTest {

  private AttributeAggregatorConfiguration subject;

  @Before
  public void before() throws Exception {
    this.doBefore("classpath:/attributeAuthoritiesProductionTemplate.yml");
  }

  private void doBefore(String configFileLocation) {
    subject = new AttributeAggregatorConfiguration();
    setField(subject, "authorityResolver", new AuthorityResolver(new DefaultResourceLoader(), configFileLocation));
    setField(subject, "environment", "test.surfconext");
    setField(subject, "cacheDuration", 1200000L);
    setField(subject, "authorizationAccessTokenUrl", "http://localhost:8889/oauth/token");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testAttributeAggregatorService() throws Exception {
    AttributeAggregatorService attributeAggregatorService = subject.attributeAggregatorService();
    Map<String, AttributeAggregator> aggregators = (Map<String, AttributeAggregator>) getField(attributeAggregatorService, "aggregators");

    assertEquals(4, aggregators.size());
    asList("orcid", "sab", "voot", "test:mock").forEach(authorityId -> assertEquals(authorityId, aggregators.get(authorityId).getAttributeAuthorityId()));
  }

  @Test(expected = IllegalArgumentException.class)
  @SuppressWarnings("unchecked")
  public void testAttributeAggregatorServiceIllegalAuthorityId() throws Exception {
    this.doBefore("classpath:/testAttributeAuthorities.yml");
    subject.attributeAggregatorService();
  }

}