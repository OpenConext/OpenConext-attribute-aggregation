package aa.aggregators.idin;

import aa.model.AttributeAuthorityConfiguration;
import aa.model.RequiredInputAttribute;
import aa.model.UserAttribute;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static aa.aggregators.AttributeAggregator.EDU_PERSON_AFFILIATION;
import static aa.aggregators.AttributeAggregator.EMAIL;
import static aa.aggregators.AttributeAggregator.IS_MEMBER_OF;
import static aa.aggregators.AttributeAggregator.NAME_ID;
import static aa.util.StreamUtils.singletonCollector;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class IdinAttributeAggregatorTest {

    private IdinAttributeAggregator subject;

    private List<UserAttribute> input = singletonList(new UserAttribute(NAME_ID, singletonList("urn:collab:person:idin.nl:confirmed")));

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    @Before
    public void before() {
        AttributeAuthorityConfiguration configuration = new AttributeAuthorityConfiguration("iden");
        configuration.setEndpoint("http://localhost:8889/api/user");
        configuration.setRequiredInputAttributes(singletonList(new RequiredInputAttribute(NAME_ID)));
        subject = new IdinAttributeAggregator(configuration);
    }

    @Test
    public void testAggregateNotFound() throws Exception {
        List<UserAttribute> idinResponse = getIdinResponse(null, 404);
        assertTrue(idinResponse.isEmpty());
    }

    @Test(expected = HttpClientErrorException.class)
    public void testAggregateException() throws Exception {
        getIdinResponse(null, 401);
    }

    @Test
    public void testAggregateUser() throws Exception {
        List<UserAttribute> idenResponse = getIdinResponse("iden/response_succes.json");

        List<String> email = userAttributeValues(idenResponse, EMAIL);
        assertEquals(email, singletonList("jdoe@example.com"));

        List<String> affiliations = userAttributeValues(idenResponse, EDU_PERSON_AFFILIATION);
        assertEquals(affiliations, asList("researcher", "student"));

        List<String> memberOf = userAttributeValues(idenResponse, IS_MEMBER_OF);
        assertEquals(memberOf, singletonList("surf.nl"));
    }

    private List<UserAttribute> getIdinResponse(String jsonFile) throws IOException {
        return getIdinResponse(jsonFile, 200);
    }

    private List<UserAttribute> getIdinResponse(String jsonFile, int status) throws IOException {
        ResponseDefinitionBuilder builder = aResponse().withStatus(status).withHeader("Content-Type", "application/json");

        if (status == 200) {
            builder = builder.withBody(IOUtils.toString(new ClassPathResource(jsonFile).getInputStream(), Charset.defaultCharset()));
        }

        stubFor(get(urlPathMatching("/api/user/.*")).willReturn(builder));
        return subject.aggregate(input);
    }

    private List<String> userAttributeValues(List<UserAttribute> userAttributes, String name) {
        return userAttributes.stream().filter(attr -> attr.getName().equals(name)).collect(singletonCollector()).getValues();
    }

}