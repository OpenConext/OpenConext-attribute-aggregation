package aa;


import aa.model.Aggregation;
import aa.model.Attribute;
import aa.repository.AggregationRepository;
import aa.repository.AttributeRepository;
import aa.repository.ServiceProviderRepository;
import aa.web.PrePopulatedJsonHttpHeaders;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpHeaders.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.context.jdbc.SqlConfig.ErrorMode.FAIL_ON_ERROR;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

/**
 * Override the @WebIntegrationTest annotation if you don't want to have mock shibboleth headers (e.g. you want to
 * impersonate EB or other identity).
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=dev",
    "attribute.authorities.config.path=classpath:testAttributeAuthorities.yml"})
@Transactional
@Sql(scripts = {"classpath:sql/clear.sql", "classpath:sql/seed.sql"},
    config = @SqlConfig(errorMode = FAIL_ON_ERROR, transactionMode = ISOLATED))
public abstract class AbstractIntegrationTest {

  protected static final String spEntityID = "http://mock-sp";

  @Autowired
  protected ServiceProviderRepository serviceProviderRepository;

  @Autowired
  protected AggregationRepository aggregationRepository;

  @Autowired
  protected AttributeRepository attributeRepository;

  @Value("${local.server.port}")
  protected int port;

  protected TestRestTemplate restTemplate;

  protected HttpHeaders headers = new PrePopulatedJsonHttpHeaders();

  @Before
  public void before() throws Exception {
    restTemplate = isBasicAuthenticated() ? new TestRestTemplate("eb", "secret") : new TestRestTemplate();
  }

  protected boolean isBasicAuthenticated() {
    return false;
  }

  protected void assertAggregations(Collection<Aggregation> aggregations) {
    assertEquals(1, aggregations.size());

    Aggregation aggregation = aggregations.iterator().next();
    assertEquals("test aggregation", aggregation.getName());

    Set<Attribute> attributes = aggregation.getAttributes();
    assertEquals(1, attributes.size());

    Attribute attribute = attributes.iterator().next();
    assertEquals(attribute.getName(), "urn:mace:dir:attribute-def:eduPersonOrcid");
    assertEquals(attribute.getAttributeAuthorityId(), "aa1");
  }

  protected HttpHeaders oauthHeaders(String accessToken) {
    HttpHeaders oauthHeaders = new HttpHeaders();
    oauthHeaders.add(ACCEPT, APPLICATION_JSON_VALUE);
    oauthHeaders.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
    oauthHeaders.add(AUTHORIZATION, "Bearer " + accessToken);
    return oauthHeaders;
  }


}
