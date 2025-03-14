package aa.aggregators.voot;

import aa.model.AttributeAuthorityConfiguration;
import aa.model.RequiredInputAttribute;
import aa.model.UserAttribute;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import static aa.aggregators.AttributeAggregator.IS_MEMBER_OF;
import static aa.aggregators.AttributeAggregator.NAME_ID;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.Collections.singletonList;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VootAttributeAggregatorTest {

    private VootAttributeAggregator subject;

    private List<UserAttribute> input = singletonList(new UserAttribute(NAME_ID, singletonList("urn")));

    private String accessTokenResponse = "{ \"access_token\"  : \"378389ea-ff94-494e-8ec6-3b0e62659bef\"}";

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
        stubFor(post(urlEqualTo("/authorize")).withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
            .willReturn(aResponse().withStatus(200).withBody(accessTokenResponse).withHeader("Content-Type", "application/json"))
        );
    }

    @Test
    public void testGetGroupsHappyFlow() throws Exception {
        String response = read("voot/groups.json");
        stubForVoot(response);
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

    /*
     * See //http://wiremock.org/stateful-behaviour.html for scenario suppurt in WireMock
     */
    @Test
    public void testGetGroupsTokenNotFoundRetry() throws Exception {
        //first we do the normal succes flow where a token is obtained
        testGetGroupsHappyFlow();

        //ask for groups again, but now throw 401 - access token is not valid - the first time
        String response = read("voot/invalid_token.json");
        stubForVootInScenario(response, 401, "first_call_done", "token_invalid_call_done");

        String correctResponse = read("voot/groups.json");
        stubForVootInScenario(correctResponse, 200, "token_invalid_call_done", "exit");

        List<UserAttribute> userAttributes = subject.aggregate(input, Collections.emptyMap());
        assertEquals(14, userAttributes.get(0).getValues().size());
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