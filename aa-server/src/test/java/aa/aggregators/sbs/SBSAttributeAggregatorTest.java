package aa.aggregators.sbs;

import aa.model.AttributeAuthorityConfiguration;
import aa.model.RequiredInputAttribute;
import aa.model.UserAttribute;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import static aa.aggregators.AttributeAggregator.EDU_PERSON_PRINCIPAL_NAME;
import static aa.aggregators.AttributeAggregator.IS_MEMBER_OF;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Collections.singletonList;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;

public class SBSAttributeAggregatorTest {

    private SBSAttributeAggregator subject;

    private List<UserAttribute> input = singletonList(new UserAttribute(EDU_PERSON_PRINCIPAL_NAME, singletonList("urn:john")));

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    @Before
    public void before() {
        AttributeAuthorityConfiguration configuration = new AttributeAuthorityConfiguration("sbs");
        configuration.setUser("user");
        configuration.setPassword("password");
        configuration.setEndpoint("http://localhost:8889/attribute_aggregation");
        configuration.setRequiredInputAttributes(singletonList(new RequiredInputAttribute(EDU_PERSON_PRINCIPAL_NAME)));
        subject = new SBSAttributeAggregator(configuration);
    }

    @Test
    public void testGetMembershipsHappyFlow() throws Exception {
        String response = read("sbs/memberships.json");
        stubForSBS(response);
        List<UserAttribute> userAttributes = subject.aggregate(input, Collections.emptyMap());
        assertEquals(1, userAttributes.size());
        UserAttribute userAttribute = userAttributes.get(0);
        assertEquals(IS_MEMBER_OF, userAttribute.getName());

        List<String> values = userAttribute.getValues();
        assertEquals(2, values.size());

    }

    @Test
    public void testGetMembershipsEmpty() throws Exception {
        String response = read("sbs/empty_memberships.json");
        stubForSBS(response);
        List<UserAttribute> userAttributes = subject.aggregate(input, Collections.emptyMap());
        assertEquals(0, userAttributes.size());
    }

    @Test
    public void testGetMemberships404() throws Exception {
        stubForSBSNotFound();
        List<UserAttribute> userAttributes = subject.aggregate(input, Collections.emptyMap());
        assertEquals(0, userAttributes.size());
    }

    private void stubForSBS(String response) {
        stubFor(get(urlPathEqualTo("/attribute_aggregation"))
                .withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(response)));
    }

    private void stubForSBSNotFound() {
        stubFor(get(urlPathEqualTo("/attribute_aggregation"))
                .withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
                .willReturn(aResponse().withStatus(404)));
    }

    private String read(String path) throws IOException {
        return IOUtils.toString(new ClassPathResource(path).getInputStream(), Charset.defaultCharset());
    }
}