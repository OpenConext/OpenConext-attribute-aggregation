package aa.control;

import aa.AbstractIntegrationTest;
import aa.model.Aggregation;
import aa.model.Attribute;
import aa.model.ServiceProvider;
import aa.util.StreamUtils;
import org.junit.Test;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static aa.util.StreamUtils.listFromIterable;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.*;
import static org.springframework.http.HttpMethod.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=no-csrf,aa-test", "attribute.authorities.config.path=classpath:testAttributeAuthorities.yml"})
public class InternalAggregationControllerTest extends AbstractIntegrationTest {

  @Override
  protected boolean isBasicAuthenticated() {
    return false;
  }

  @Test
  public void testSaveAggregation() throws Exception {
    ResponseEntity<String> result = successfulPostAggregation("test-new-aggregation", POST);

    assertEquals(HttpStatus.OK, result.getStatusCode());

    List<Aggregation> aggregations = listFromIterable(aggregationRepository.findAll());
    assertEquals(2, aggregations.size());

    ServiceProvider serviceProvider = serviceProviderRepository.findByEntityId("https://oidc.test.surfconext.nl").get();

    assertEquals(1, serviceProvider.getAggregations().size());

    Aggregation fromDbAggregation = serviceProvider.getAggregations().iterator().next();
    assertEquals(3, fromDbAggregation.getAttributes().size());

    List<String> authorities = fromDbAggregation.getAttributes().stream().map(Attribute::getAttributeAuthorityId).sorted().collect(toList());
    assertEquals(Arrays.asList("aa1", "aa1", "aa2"), authorities);
  }

  @Test
  public void testUpdateAggregation() throws Exception {
    Aggregation aggregation = findAggregationById(1L);
    assertEquals(1L, aggregation.getId().longValue());

    aggregation.setName("new-name");
    aggregation.setServiceProviders(new HashSet<>(singletonList(new ServiceProvider("https://oidc.localhost.surfconext.nl"))));

    Attribute attribute = new Attribute("urn:mace:dir:attribute-def:eduPersonEntitlement", "aa1");
    attribute.setSkipConsent(true);
    aggregation.setAttributes(new HashSet<>(singletonList(attribute)));

    RequestEntity requestEntity = new RequestEntity(aggregation, headers, PUT, new URI("http://localhost:" + port + "/aa/api/internal/aggregation"));
    ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    Aggregation savedAggregation = findAggregationById(1L);

    assertEquals(aggregation.getName(), savedAggregation.getName());
    assertEquals(aggregation.getServiceProviders(), savedAggregation.getServiceProviders());

    assertTrue(savedAggregation.getAttributes().iterator().next().isSkipConsent());
  }

  @Test
  public void deleteAggregationAndOrphanedServiceProvider() throws Exception {
    RequestEntity requestEntity = new RequestEntity(headers, DELETE, new URI("http://localhost:" + port + "/aa/api/internal/aggregation/1"));
    ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());

    ResponseEntity<List<Aggregation>> result = findAllAggregations();
    assertEquals(0, result.getBody().size());

    //orphaned SP cleaned up
    assertEquals(0, listFromIterable(serviceProviderRepository.findAll()).size());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testFindAll() throws Exception {
    successfulPostAggregation("test2-aggregation", POST);
    successfulPostAggregation("test3-aggregation", POST);

    ResponseEntity<List<Aggregation>> result = findAllAggregations();

    assertEquals(HttpStatus.OK, result.getStatusCode());

    List<Aggregation> aggregations = result.getBody();
    List<String> names = aggregations.stream().map(Aggregation::getName).sorted().collect(toList());
    //the first one is from AbstractIntegrationTest@SQL seed script
    assertEquals(Arrays.asList("test aggregation", "test2-aggregation", "test3-aggregation"), names);

    Aggregation aggregation = aggregations.stream().filter(aggr -> aggr.getName().equals("test2-aggregation")).collect(StreamUtils.singletonCollector());
    Set<ServiceProvider> serviceProviders = aggregation.getServiceProviders();
    assertEquals(2, serviceProviders.size());
    serviceProviders.forEach(sp -> assertNotNull(sp.getName()));
    serviceProviders.forEach(sp -> assertNotNull(sp.getDescription()));
    assertEquals(3, aggregation.getAttributes().size());
  }

  @Test
  public void testAggregationBadRequest() throws Exception {
    ResponseEntity<String> result = postAggregation(
        "test-aggregation",
        singletonList(
            new Attribute("bogus", "aa1")),
        singletonList(
            new ServiceProvider("http://mock-sp")), POST);

    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  public void testAggregationExistsByName() throws Exception {
    assertTrue(aggregationExistsByName("TEST AGGREGATION"));
    assertFalse(aggregationExistsByName("nope"));
  }

  @Test
  public void testAggregationExistsByNameAndId() throws Exception {
    assertFalse(aggregationExistsByNameAndId("TEST AGGREGATION", 1L));
    assertTrue(aggregationExistsByNameAndId("TEST AGGREGATION", 2L));
  }

  @Test
  public void testAggregationsByServiceProviderEntityIds() throws Exception {
    URI uri = fromHttpUrl("http://localhost:" + port + "/aa/api/internal/aggregationsByServiceProviderEntityIds")
        .queryParam("entityIds", "http://mock-sp", "http://unknown-sp").build().encode().toUri();
    RequestEntity requestEntity = new RequestEntity(headers, GET, uri);

    String s = restTemplate.exchange(requestEntity,String.class).getBody();


    List<Object[]> names = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<List<Object[]>>() {
    }).getBody();
    assertEquals(1, names.size());
    assertEquals("test aggregation", names.get(0)[0]);
    assertEquals("http://mock-sp", names.get(0)[1]);

  }

  @Test
  public void testFindAggregationById() throws URISyntaxException {
    RequestEntity requestEntity = new RequestEntity(headers, GET, new URI("http://localhost:" + port + "/aa/api/internal/aggregation/99"));
    assertEquals(NOT_FOUND, restTemplate.exchange(requestEntity, String.class).getStatusCode());
  }

  private boolean aggregationExistsByName(String name) {
    return aggregationExists(fromHttpUrl("http://localhost:" + port + "/aa/api/internal/aggregationExistsByName")
        .queryParam("name", name).build().encode().toUri());
  }

  private boolean aggregationExistsByNameAndId(String name, Long id) {
    return aggregationExists(fromHttpUrl("http://localhost:" + port + "/aa/api/internal/aggregationExistsByName")
        .queryParam("name", name).queryParam("id", id).build().encode().toUri());
  }

  private boolean aggregationExists(URI uri) {
    RequestEntity requestEntity = new RequestEntity(headers, GET, uri);
    return restTemplate.exchange(requestEntity, Boolean.class).getBody();
  }

  private ResponseEntity<List<Aggregation>> findAllAggregations() throws URISyntaxException {
    RequestEntity requestEntity = new RequestEntity(headers, GET, new URI("http://localhost:" + port + "/aa/api/internal/aggregations"));
    return restTemplate.exchange(requestEntity, new ParameterizedTypeReference<List<Aggregation>>() {
    });
  }

  private Aggregation findAggregationById(Long id) throws URISyntaxException {
    RequestEntity requestEntity = new RequestEntity(headers, GET, new URI("http://localhost:" + port + "/aa/api/internal/aggregation/" + id));
    return restTemplate.exchange(requestEntity, Aggregation.class).getBody();
  }

  private ResponseEntity<String> successfulPostAggregation(String name, HttpMethod method) throws URISyntaxException {
    return postAggregation(
        name,
        Arrays.asList(
            new Attribute("urn:mace:dir:attribute-def:eduPersonOrcid", "aa1"),
            new Attribute("urn:mace:dir:attribute-def:eduPersonEntitlement", "aa1"),
            new Attribute("urn:mace:dir:attribute-def:eduPersonEntitlement", "aa2")),
        Arrays.asList(
            new ServiceProvider("http://mock-sp"),
            new ServiceProvider("https://oidc.test.surfconext.nl")), method);
  }

  @SuppressWarnings("unchecked")
  private ResponseEntity<String> postAggregation(String name, List<Attribute> attributest, List<ServiceProvider> serviceProviders, HttpMethod method) throws URISyntaxException {
    Aggregation aggregation = new Aggregation();
    aggregation.setName(name);
    aggregation.setAttributes(new HashSet<>(attributest));
    aggregation.setServiceProviders(new HashSet<>(serviceProviders));

    RequestEntity requestEntity = new RequestEntity(aggregation, headers, method, new URI("http://localhost:" + port + "/aa/api/internal/aggregation"));
    return restTemplate.exchange(requestEntity, String.class);
  }

}