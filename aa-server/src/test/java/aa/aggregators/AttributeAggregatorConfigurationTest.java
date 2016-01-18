package aa.aggregators;

import aa.config.AuthorityResolver;
import aa.service.AttributeAggregatorService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.springframework.test.util.ReflectionTestUtils.getField;
import static org.springframework.test.util.ReflectionTestUtils.setField;

public class AttributeAggregatorConfigurationTest {

  private AttributeAggregatorConfiguration subject;

  @Before
  public void before() throws Exception {
    subject = new AttributeAggregatorConfiguration();
    setField(subject, "authorityResolver", new AuthorityResolver(new DefaultResourceLoader(), "classpath:/realWorldAttributeAuthorities.yml"));
    setField(subject, "environment", "test.surfconext");
    setField(subject, "cacheDuration", 1200000L);
    setField(subject, "authorizationAccessTokenUrl", "http://localhost:8889/oauth/token");
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testAttributeAggregatorService() throws Exception {
    AttributeAggregatorService attributeAggregatorService = subject.attributeAggregatorService();
    Map<String, AttributeAggregator> aggregators = (Map<String, AttributeAggregator>) getField(attributeAggregatorService, "aggregators");

    assertEquals(3, aggregators.size());
    asList("orcid", "sab", "voot").forEach(authorityId -> assertEquals(authorityId, aggregators.get(authorityId).getAttributeAuthorityId()));
  }
}