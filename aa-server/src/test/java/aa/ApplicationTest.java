package aa;

import org.junit.Test;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.Assert.assertEquals;

@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=dev", "attribute.authorities.config.path=classpath:attributeAuthorities.yml"})
public class ApplicationTest extends AbstractIntegrationTest {

  @Test
  public void startUp() {
    ResponseEntity<Map> response = restTemplate.getForEntity("http://localhost:" + port + "/aa/api/health", Map.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals(response.getBody().get("status"), "UP");
  }

}