package aa.control;

import aa.AbstractIntegrationTest;
import aa.model.ArpAggregationRequest;
import aa.model.ArpValue;
import aa.model.UserAttribute;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static aa.aggregators.AttributeAggregator.*;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        value = {"attribute_authorities_config_path=classpath:testAttributeAuthorities.yml"})
public class AttributeAggregatorControllerTest extends AbstractIntegrationTest {

    @Override
    protected boolean isBasicAuthenticated() {
        return true;
    }

    @Test
    public void testAggregateWithArp() throws Exception {
        UserAttribute input = new UserAttribute(EDU_PERSON_PRINCIPAL_NAME, singletonList("urn:collab:person:example.com:admin"));
        Map<String, List<ArpValue>> arp = new HashMap<>();
        arp.put(ORCID, Arrays.asList(new ArpValue("*", "aa1")));
        arp.put(EDU_PERSON_ENTITLEMENT, Arrays.asList(new ArpValue("nope", "aa1")));
        ArpAggregationRequest arpAggregationRequest = new ArpAggregationRequest(singletonList(input), arp);

        RequestEntity<ArpAggregationRequest> requestEntity = new RequestEntity<>(arpAggregationRequest, headers, HttpMethod.POST,
                new URI("http://localhost:" + port + "/aa/api/internal/attribute/aggregation"));

        ResponseEntity<String> re = restTemplate.exchange(requestEntity, String.class);

        ResponseEntity<List<UserAttribute>> response = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>() {
        });

        List<UserAttribute> userAttributes = response.getBody();
        assertEquals(1, userAttributes.size());

        UserAttribute userAttribute = userAttributes.get(0);
        assertEquals(ORCID, userAttribute.getName());
        assertEquals(singletonList("urn:x-surfnet:aa1:test"), userAttribute.getValues());
        assertEquals("aa1", userAttribute.getSource());

    }

    @Test
    public void testAggregatedWithFaultyJson() throws Exception {
        String json = IOUtils.toString(new ClassPathResource("json/eb/faulty_request_eb.json").getURL(), Charset.defaultCharset());
        RequestEntity<String> requestEntity = new RequestEntity<>(json, headers, HttpMethod.POST,
                new URI("http://localhost:" + port + "/aa/api/client/attribute/aggregation"));

        ResponseEntity<String> re = restTemplate.exchange(requestEntity, String.class);
        assertEquals(400, re.getStatusCode().value());

    }

}