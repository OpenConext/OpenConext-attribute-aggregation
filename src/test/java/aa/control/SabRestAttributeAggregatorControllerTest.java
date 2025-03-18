package aa.control;

import aa.AbstractIntegrationTest;
import aa.model.ArpAggregationRequest;
import aa.model.ArpValue;
import aa.model.UserAttribute;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static aa.aggregators.AttributeAggregator.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        value = {"attribute_authorities_config_path=classpath:testSabRestAttributeAuthority.yml"})
@ActiveProfiles(profiles = {"prod"}, inheritProfiles = false)
public class SabRestAttributeAggregatorControllerTest extends AbstractIntegrationTest {

    @Override
    protected boolean isBasicAuthenticated() {
        return true;
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    @Test
    public void testSabRegExp() throws IOException, URISyntaxException {
        String stubResponse = IOUtils.toString(new ClassPathResource("sabrest/response_success.json").getInputStream(), Charset.defaultCharset());
        stubFor(get(urlPathEqualTo("/api/profile")).willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(stubResponse)));

        Map<String, List<ArpValue>> arp = Collections.singletonMap(EDU_PERSON_ENTITLEMENT,
                List.of(new ArpValue("*", "sabrest")));
        ArpAggregationRequest arpAggregationRequest = new ArpAggregationRequest(List.of(
                new UserAttribute(UID, singletonList("uid")),
                new UserAttribute(SCHAC_HOME_ORGANIZATION, singletonList("example.com"))
        ), arp);

        RequestEntity<ArpAggregationRequest> requestEntity = new RequestEntity<>(arpAggregationRequest, headers, HttpMethod.POST,
                new URI("http://localhost:" + port + "/aa/api/internal/attribute/aggregation"));

        ResponseEntity<List<UserAttribute>> response = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<>() {
        });

        List<UserAttribute> userAttributes = response.getBody();
        assertEquals(5, userAttributes.get(0).getValues().size());
    }

}