package aa.control;

import aa.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AppInformationControllerTest extends AbstractIntegrationTest {

    @Test
    public void testUser() throws Exception {
        RequestEntity requestEntity = new RequestEntity(headers, HttpMethod.GET, new URI("http://localhost:" + port + "/aa/api/internal/appInformation"));
        ResponseEntity<Map> response = restTemplate.exchange(requestEntity, Map.class);

        Map body = response.getBody();
        assertEquals(body.get("version"), "1.0.1-SNAPSHOT");
    }
}