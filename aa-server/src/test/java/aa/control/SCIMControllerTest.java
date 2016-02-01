package aa.control;

import aa.model.MetaInformation;
import aa.model.ResourceType;
import aa.model.Schema;
import aa.oidc.AbstractOidcIntegrationTest;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

public class SCIMControllerTest extends AbstractOidcIntegrationTest {

  @Test
  public void testSchemaEndpoint() throws Exception {
    ResponseEntity<Schema> response = getSchemaResponse("json/oidc/introspect.client_credentials.json");
    assertEquals(OK, response.getStatusCode());

    Schema schema = response.getBody();
    assertSchema(schema);
  }

  @Test
  public void testSchemaEndpointNoSp() throws Exception {
    ResponseEntity<Schema> response = getSchemaResponse("json/oidc/introspect.client_credentials.unknown_sp.json");
    assertEquals(NOT_FOUND, response.getStatusCode());
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testServiceProviderConfiguration() throws Exception {
    Map body = restTemplate.exchange(new RequestEntity(headers, GET, new URI("http://localhost:" + port + "/aa/api/v2/ServiceProviderConfig")), Map.class).getBody();
    assertEquals("https://aa.test.surfconext.nl/v2/ServiceProviderConfig", ((Map) body.get("meta")).get("location"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testResourceType() throws Exception {
    RequestEntity requestEntity = new RequestEntity(oauthHeaders, GET, new URI("http://localhost:" + port + "/aa/api/v2/ResourceType"));
    ResponseEntity<ResourceType> responseEntity = restTemplate.exchange(requestEntity, ResourceType.class);
    assertResourceType(responseEntity.getBody());
  }

  @Test
  public void testMe() throws Exception {
    RequestEntity requestEntity = new RequestEntity(oauthHeaders, GET, new URI("http://localhost:" + port + "/aa/api/v2/Me"));
    ResponseEntity<Map<String, Object>> result = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
    });
    assertMeResult(result.getBody());
  }

  private ResponseEntity<Schema> getSchemaResponse(String file) throws IOException, URISyntaxException {
    //Schema endPoint is for client credentials
    stubOidcCheckTokenUser(file);

    RequestEntity requestEntity = new RequestEntity(oauthHeaders, GET, new URI("http://localhost:" + port + "/aa/api/v2/Schema"));
    return restTemplate.exchange(requestEntity, Schema.class);
  }

}