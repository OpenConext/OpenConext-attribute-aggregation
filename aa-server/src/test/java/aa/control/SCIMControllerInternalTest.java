package aa.control;

import aa.AbstractIntegrationTest;
import aa.model.ResourceType;
import aa.model.Schema;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static aa.aggregators.AttributeAggregator.EDU_PERSON_PRINCIPAL_NAME;
import static aa.aggregators.AttributeAggregator.NAME_ID;
import static aa.aggregators.AttributeAggregator.SCHAC_HOME;
import static java.net.URLEncoder.encode;
import static org.junit.Assert.assertEquals;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/*
 * Need separate Controller because of security environment. We want ADMIN rights here and not use OAuth. We also
 * don't want CSRF headers
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    value = {"spring.profiles.active=dev,no-csrf,aa-test",
        "attribute.authorities.config.path=classpath:testAttributeAuthorities.yml"})
public class SCIMControllerInternalTest extends AbstractIntegrationTest {

    @Test
    public void testInternalResourceTypeEndPoint() throws Exception {
        RequestEntity requestEntity = new RequestEntity(headers, GET, new URI("http://localhost:" + port + "/aa/api/internal/v2/ResourceType?serviceProviderEntityId=" + encode("http://mock-sp", "UTF-8")));

        ResponseEntity<ResourceType> responseEntity = restTemplate.exchange(requestEntity, ResourceType.class);
        assertResourceType(responseEntity.getBody());
    }

    @Test
    public void testInternalResourceTypeEndPointUnknownServiceProvider() throws Exception {
        RequestEntity requestEntity = new RequestEntity(headers, GET, new URI("http://localhost:" + port + "/aa/api/internal/v2/ResourceType?serviceProviderEntityId=" + encode("http://unknown-sp", "UTF-8")));

        assertEquals(NOT_FOUND, restTemplate.exchange(requestEntity, String.class).getStatusCode());
    }

    @Test
    public void testInternalSchemaEndPoint() throws Exception {
        RequestEntity requestEntity = new RequestEntity(headers, GET, new URI("http://localhost:" + port + "/aa/api/internal/v2/Schema?serviceProviderEntityId=" + encode("http://mock-sp", "UTF-8")));

        ResponseEntity<Schema> responseEntity = restTemplate.exchange(requestEntity, Schema.class);
        Schema schema = responseEntity.getBody();

        assertSchema(schema);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testInternalServiceProviderConfiguration() throws Exception {
        Map body = restTemplate.exchange(new RequestEntity(headers, GET, new URI("http://localhost:" + port + "/aa/api/internal/v2/ServiceProviderConfig")), Map.class).getBody();
        assertEquals("https://aa.test.surfconext.nl/v2/ServiceProviderConfig", ((Map) body.get("meta")).get("location"));
    }


    @Test
    public void testMe() throws Exception {
        Map<String, String> inputParameters = new HashMap<>();
        inputParameters.put(NAME_ID, "test");
        inputParameters.put(EDU_PERSON_PRINCIPAL_NAME, "test");
        inputParameters.put(SCHAC_HOME, "test");

        RequestEntity requestEntity = new RequestEntity(inputParameters, headers, POST, new URI("http://localhost:" + port + "/aa/api/internal/v2/Me?serviceProviderEntityId=" + encode("http://mock-sp", "UTF-8")));
        ResponseEntity<Map<String, Object>> result = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<Map<String, Object>>() {
        });
        assertMeResult(result.getBody());
    }

    @Test
    public void testMeUnknownServiceProvider() throws Exception {
        Map<String, String> inputParameters = new HashMap<>();
        RequestEntity requestEntity = new RequestEntity(inputParameters, headers, POST, new URI("http://localhost:" + port + "/aa/api/internal/v2/Me?serviceProviderEntityId=" + encode("http://unknown-sp", "UTF-8")));
        assertEquals(NOT_FOUND, restTemplate.exchange(requestEntity, String.class).getStatusCode());
    }

}