package aa.control;

import aa.AbstractIntegrationTest;
import aa.config.AuthorityConfiguration;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;

import static org.junit.Assert.assertEquals;

public class AuthorityConfigurationControllerTest extends AbstractIntegrationTest {

  @Override
  protected boolean isBasicAuthenticated() {
    return false;
  }

  @Test
  public void testAuthorityConfiguration() throws Exception {
    RequestEntity requestEntity = new RequestEntity(headers, HttpMethod.GET, new URI("http://localhost:" + port + "/aa/api/internal/authorityConfiguration"));
    ResponseEntity<AuthorityConfiguration> response = restTemplate.exchange(requestEntity, AuthorityConfiguration.class);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    AuthorityConfiguration authorityConfiguration = response.getBody();

    assertEquals(2, authorityConfiguration.getAuthorityById("aa1").getAttributes().size());
    assertEquals(1, authorityConfiguration.getAuthorityById("aa2").getAttributes().size());
  }

}