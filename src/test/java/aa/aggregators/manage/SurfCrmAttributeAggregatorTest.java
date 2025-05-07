package aa.aggregators.manage;

import aa.model.ArpAggregationRequest;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.RequiredInputAttribute;
import aa.model.UserAttribute;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import static aa.aggregators.AttributeAggregator.IDP_ENTITY_ID;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static java.util.Collections.singletonList;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;

public class SurfCrmAttributeAggregatorTest {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final ArpAggregationRequest arpAggregationRequest = objectMapper.readValue(new ClassPathResource("manage/arp_aggregation_request.json").getInputStream(),
            ArpAggregationRequest.class);

    private SurfCrmAttributeAggregator subject;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    public SurfCrmAttributeAggregatorTest() throws IOException {
    }

    @Before
    public void before() {
        AttributeAuthorityConfiguration configuration = new AttributeAuthorityConfiguration("manage");
        configuration.setUser("user");
        configuration.setPassword("password");
        configuration.setEndpoint("http://localhost:8889");
        configuration.setRequiredInputAttributes(singletonList(new RequiredInputAttribute(IDP_ENTITY_ID)));
        subject = new SurfCrmAttributeAggregator(configuration);
    }

    @Test
    public void aggregate() throws IOException {
        stubForManage(read("manage/result.json"));
        List<UserAttribute> userAttributes = subject.aggregate(
                arpAggregationRequest.getUserAttributes(),
                arpAggregationRequest.getArpAttributes());

        assertEquals(1, userAttributes.size());

        UserAttribute userAttribute = userAttributes.get(0);
        assertEquals("manage", userAttribute.getSource());
        assertEquals(1, userAttribute.getValues().size());

        assertEquals("ad93daef-0911-e511-80d0-005056956c1a", userAttribute.getValues().get(0));
    }

    @Test
    public void aggregateEmptyResult() throws IOException {
        stubForManage(read("manage/empty_result.json"));
        List<UserAttribute> userAttributes = subject.aggregate(
                arpAggregationRequest.getUserAttributes(),
                arpAggregationRequest.getArpAttributes());

        assertEquals(0, userAttributes.size());
    }

    @Test
    public void aggregateResultWithNo() throws IOException {
        stubForManage(read("manage/empty_result.json"));
        List<UserAttribute> userAttributes = subject.aggregate(
                arpAggregationRequest.getUserAttributes(),
                arpAggregationRequest.getArpAttributes());

        assertEquals(0, userAttributes.size());
    }

    private void stubForManage(String response) {
        stubFor(post(urlPathEqualTo("/manage/api/internal/search/saml20_idp"))
                .withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(response)));
    }

    private String read(String path) throws IOException {
        return IOUtils.toString(new ClassPathResource(path).getInputStream(), Charset.defaultCharset());
    }
}