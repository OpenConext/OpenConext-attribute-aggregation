package aa.control;

import aa.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GitPluginControllerTest extends AbstractIntegrationTest {

  @Test
  public void testGit() throws Exception {
    ResponseEntity<Map> response = restTemplate.getForEntity("http://localhost:" + port + "/aa/api/public/git", Map.class);
    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertTrue(response.getBody().containsKey("git.commit.id"));
  }
}