package aa;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, value = {"spring.profiles.active=dev,aa-test", "attribute_authorities_config_path=classpath:attributeAuthorities.yml"})
public class ApplicationTest extends AbstractIntegrationTest {

    @Test
    public void startUp() {
        ResponseEntity<Map> response = restTemplate.getForEntity("http://localhost:" + port + "/aa/api/health", Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(response.getBody().get("status"), "UP");
    }

    @Test
    public void testMain() {
        Application application = new Application();
        application.main(new String[]{});
    }

}