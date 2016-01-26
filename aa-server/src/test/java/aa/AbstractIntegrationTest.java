package aa;


import aa.model.*;
import aa.repository.AggregationRepository;
import aa.repository.ServiceProviderRepository;
import aa.web.PrePopulatedJsonHttpHeaders;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.TestRestTemplate;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.*;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=dev,aa-test",
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

  protected void assertSchema(Schema schema) {
    assertEquals(spEntityID, schema.getName());
    assertEquals("Attribute schema for " + spEntityID, schema.getDescription());
    assertEquals("urn:scim:schemas:extension:surf:" + spEntityID, schema.getId());

    assertEquals(1, schema.getAttributes().size());
    Attribute attribute = schema.getAttributes().get(0);
    //we don't want this field in the Schema as it is not a valid SCIM attribute
    assertEquals(null, attribute.getAttributeAuthorityId());
    assertEquals("urn:mace:dir:attribute-def:eduPersonOrcid", attribute.getName());

    assertEquals("readOnly", attribute.getMutability());
  }

  protected void assertResourceType(ResourceType resourceType) {
    assertEquals("/Me", resourceType.getEndpoint());
    assertEquals("http://mock-sp", resourceType.getId());
    assertEquals("http://mock-sp", resourceType.getName());
    assertEquals("urn:scim:schemas:extension:surf:http://mock-sp", resourceType.getSchema());

    List<String> schemas = resourceType.getSchemas();
    assertEquals(1, schemas.size());
    assertEquals("urn:ietf:params:scim:schemas:core:2.0:ResourceType", schemas.get(0));

    MetaInformation metaInformation = resourceType.getMeta();
    assertEquals("https://aa.test.surfconext.nl/v2/ResourceTypes/Me", metaInformation.getLocation());
    assertEquals("ResourceType", metaInformation.getResourceType());
  }

  protected void assertMeResult(Map<String, Object> body) {
    assertEquals(Collections.singletonList("urn:scim:schemas:extension:surf:http://mock-sp"), body.get("schemas"));
    assertNotNull(UUID.fromString((String) body.get("id")));
    assertEquals(Collections.singletonList("urn:x-surfnet:aa1:test"), body.get("urn:mace:dir:attribute-def:eduPersonOrcid"));
    assertEquals(5, ((Map<String, Object>) body.get("meta")).size());
  }

  protected HttpHeaders oauthHeaders(String accessToken) {
    HttpHeaders oauthHeaders = new HttpHeaders();
    oauthHeaders.add(ACCEPT, APPLICATION_JSON_VALUE);
    oauthHeaders.add(CONTENT_TYPE, APPLICATION_JSON_VALUE);
    oauthHeaders.add(AUTHORIZATION, "Bearer " + accessToken);
    return oauthHeaders;
  }

  @SuppressWarnings("unchecked")
  protected List<UserAttribute> doAttributeAggregate(String serviceProviderEntityId, String userAttributeName, int port) throws Exception {
    UserAttribute input = new UserAttribute(userAttributeName,
        singletonList("urn:collab:person:example.com:admin"),
        null);
    UserAttributes userAttributes = new UserAttributes(serviceProviderEntityId, singletonList(input));

    RequestEntity requestEntity = new RequestEntity(userAttributes, headers, HttpMethod.POST, new URI("http://localhost:" + port + "/aa/api/attribute/aggregate"));
    ResponseEntity<List<UserAttribute>> response = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<List<UserAttribute>>() {
    });

    return response.getBody();
  }

}
