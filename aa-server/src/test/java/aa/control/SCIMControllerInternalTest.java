package aa.control;

import aa.AbstractIntegrationTest;
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
@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=dev,no-csrf",
    "attribute.authorities.config.path=classpath:testAttributeAuthorities.yml"})
public class SCIMControllerInternalTest extends AbstractIntegrationTest {

  @Test
  public void testInternalSchemaEndPoint() throws Exception {
    RequestEntity requestEntity = new RequestEntity(headers, GET, new URI("http://localhost:" + port + "/aa/api/internal/v1/Schema?serviceProviderEntityId=" + encode("http://mock-sp", "UTF-8")));

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

    RequestEntity requestEntity = new RequestEntity(inputParameters, headers, POST, new URI("http://localhost:" + port + "/aa/api/internal/v1/Me?serviceProviderEntityId=" + encode("http://mock-sp", "UTF-8")));
    ResponseEntity<Map<String, Object>> result = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
    });

    Map<String, Object> body = result.getBody();

    assertEquals(body.get("schema"), Collections.singletonList("urn:ietf:params:scim:schemas:extension:x-surfnet:http://mock-sp"));
    assertNotNull(UUID.fromString((String) body.get("id")));
    assertEquals(Collections.singletonList("urn:x-surfnet:aa1:test"), body.get("urn:mace:dir:attribute-def:eduPersonOrcid"));
    assertEquals(5, ((Map<String, Object>) body.get("meta")).size());
  }


}