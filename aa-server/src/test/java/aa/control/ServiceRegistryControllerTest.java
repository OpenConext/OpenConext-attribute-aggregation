package aa.control;

import aa.AbstractIntegrationTest;
import aa.model.ServiceProvider;
import org.junit.Test;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=acc", "attribute.authorities.config.path=classpath:testAttributeAuthorities.yml"})
public class ServiceRegistryControllerTest extends AbstractIntegrationTest {

  @Override
  protected boolean isBasicAuthenticated() {
    return true;
  }

  @Test
  public void testServiceProviders() throws Exception {
    RequestEntity requestEntity = new RequestEntity(headers, HttpMethod.GET, new URI("http://localhost:" + port + "/aa/api/internal/serviceProviders"));
    ResponseEntity<Collection<ServiceProvider>> response = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<Collection<ServiceProvider>>() {
    });

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(953, response.getBody().size());
  }

}