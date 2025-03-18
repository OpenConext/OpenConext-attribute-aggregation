package aa.aggregators.voot;

import aa.model.AttributeAuthorityConfiguration;
import aa.model.RequiredInputAttribute;
import aa.model.UserAttribute;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static aa.aggregators.AttributeAggregator.IS_MEMBER_OF;
import static aa.aggregators.AttributeAggregator.NAME_ID;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Collections.singletonList;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VootAttributeAggregatorTest {

    private VootAttributeAggregator subject;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<UserAttribute> input = singletonList(new UserAttribute(NAME_ID, singletonList("urn")));

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    @Before
    public void before() {
        AttributeAuthorityConfiguration configuration = new AttributeAuthorityConfiguration("voot");
        configuration.setUser("user");
        configuration.setPassword("password");
        configuration.setEndpoint("http://localhost:8889/voot");
        configuration.setRequiredInputAttributes(singletonList(new RequiredInputAttribute(NAME_ID)));
        subject = new VootAttributeAggregator(configuration, "http://localhost:8889/authorize");
        this.stubAccessTokenResponse();
    }

    @SneakyThrows
    private void stubAccessTokenResponse() {
        Map<String, ? extends Serializable> body = Map.of(
                "access_token", UUID.randomUUID().toString(),
                "token_type", "Bearer",
                "expires_in", 3600);
        stubFor(post(urlEqualTo("/authorize")).withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(objectMapper.writeValueAsString(body)).withHeader("Content-Type", "application/json"))
        );
    }

    @Test
    public void testGetGroupsHappyFlow() throws Exception {
        String response = read("voot/groups.json");
        stubForVoot(response);

        subject.aggregate(input, Collections.emptyMap());

        stubForVootInScenario(response, 200, "first_call_done", "second_call_done");
        //Now ensure the cached access token is used
        stubFor(post(urlEqualTo("/authorize"))
                .willReturn(aResponse()
                        .withStatus(401)
                        .withBody(objectMapper.writeValueAsString(Map.of())).withHeader("Content-Type", "application/json"))
        );

        List<UserAttribute> userAttributes = subject.aggregate(input, Collections.emptyMap());

        assertEquals(1, userAttributes.size());
        UserAttribute userAttribute = userAttributes.get(0);
        assertEquals(IS_MEMBER_OF, userAttribute.getName());

        List<String> values = userAttribute.getValues();
        assertEquals(14, values.size());

        values.forEach(value -> assertTrue(value.startsWith("urn:collab:group:test.surfteams.nl")));
    }

    @Test
    public void testGetGroupFailures() throws Exception {
        //if something goes wrong, we just don't get groups. We log all requests and responses
        String response = read("voot/empty_groups.json");
        stubForVoot(response);
        assertTrue(subject.aggregate(input, Collections.emptyMap()).isEmpty());
    }

    private void stubForVoot(String response) {
        stubForVootInScenario(response, 200, Scenario.STARTED, "first_call_done");
    }

    private void stubForVootInScenario(String response, int status, String scenarioState, String newScenarioState) {
        stubFor(get(urlEqualTo("/voot/internal/groups/urn"))
                .inScenario("voot")
                .willReturn(aResponse().withStatus(status).withBody(response).withHeader("Content-Type", "application/json"))
                .whenScenarioStateIs(scenarioState)
                .willSetStateTo(newScenarioState)
        );
    }

    private String read(String path) throws IOException {
        return IOUtils.toString(new ClassPathResource(path).getInputStream(), Charset.defaultCharset());
    }

}