package aa.control;

import aa.AbstractIntegrationTest;
import aa.model.ServiceProvider;
import aa.shibboleth.FederatedUser;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.*;

public class UserControllerTest extends AbstractIntegrationTest {

  @Test
  public void testUser() throws Exception {
    RequestEntity requestEntity = new RequestEntity(headers, HttpMethod.GET, new URI("http://localhost:" + port + "/aa/api/internal/users/me"));
    ResponseEntity<Map> response = restTemplate.exchange(requestEntity, Map.class);

    Map body = response.getBody();
    assertEquals(body.get("username"),"saml2_user");
    assertEquals(body.get("displayName"),"John Doe");
  }
}