package aa.control;

import aa.AbstractIntegrationTest;
import aa.model.ResourceType;
import aa.model.Schema;
import org.junit.Test;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.net.URLEncoder.encode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

/*
 * Need separate Controller because of security environment. We want ADMIN rights here and not use OAuth. We also
 * don't want CSRF headers
 */
@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=dev,no-csrf,aa-test",
    "attribute.authorities.config.path=classpath:testAttributeAuthorities.yml"})
public class SCIMControllerInternalTest extends AbstractIntegrationTest {


  @Test
  public void testInternalResourceTypeEndPoint() throws Exception {
    RequestEntity requestEntity = new RequestEntity(headers, GET, new URI("http://localhost:" + port + "/aa/api/internal/v2/ResourceType?serviceProviderEntityId=" + encode("http://mock-sp", "UTF-8")));

    ResponseEntity<ResourceType> responseEntity = restTemplate.exchange(requestEntity, ResourceType.class);
    assertResourceType(responseEntity.getBody());
  }

  @Test
  public void testInternalSchemaEndPoint() throws Exception {
    RequestEntity requestEntity = new RequestEntity(headers, GET, new URI("http://localhost:" + port + "/aa/api/internal/v2/Schema?serviceProviderEntityId=" + encode("http://mock-sp", "UTF-8")));

    ResponseEntity<Schema> responseEntity = restTemplate.exchange(requestEntity, Schema.class);
    Schema schema = responseEntity.getBody();

    assertSchema(schema);
  }

  @Test
  public void testMe() throws Exception {
    Map<String, String> inputParameters = new HashMap<>();
    inputParameters.put("urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified", "test");
    inputParameters.put("urn:mace:dir:attribute-def:eduPersonPrincipalName", "test");
    inputParameters.put("urn:mace:terena.org:attribute-def:schacHomeOrganization", "test");

    RequestEntity requestEntity = new RequestEntity(inputParameters, headers, POST, new URI("http://localhost:" + port + "/aa/api/internal/v2/Me?serviceProviderEntityId=" + encode("http://mock-sp", "UTF-8")));
    ResponseEntity<Map<String, Object>> result = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
    });
    assertMeResult(result.getBody());
  }


}