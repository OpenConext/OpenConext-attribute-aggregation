package aa.entitlements;

import aa.model.AttributeAuthorityConfiguration;
import aa.model.RequiredInputAttribute;
import aa.model.UserAttribute;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Arrays;
import java.util.List;

import static aa.aggregators.AttributeAggregator.EDU_PERSON_ENTITLEMENT;
import static aa.aggregators.AttributeAggregator.EDU_PERSON_PRINCIPAL_NAME;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.nio.charset.Charset.defaultCharset;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class EntitlementsAggregatorTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);
    private EntitlementsAggregator subject;
    private List<UserAttribute> input = singletonList(new UserAttribute(EDU_PERSON_PRINCIPAL_NAME, singletonList
        ("principal")));

    @Before
    public void before() {
        AttributeAuthorityConfiguration configuration = new AttributeAuthorityConfiguration("sab");
        configuration.setUser("user");
        configuration.setPassword("password");
        configuration.setEndpoint("http://localhost:8889");
        configuration.setRequiredInputAttributes(singletonList(new RequiredInputAttribute(EDU_PERSON_PRINCIPAL_NAME)));
        subject = new EntitlementsAggregator(configuration);
    }

    @Test
    public void testGetEntitlementsHappyFlow() throws Exception {
        String token = IOUtils.toString(new ClassPathResource("entitlements/token.json").getInputStream(),
            defaultCharset());
        stubFor(post(urlEqualTo("/Token")).willReturn(aResponse().withStatus(200).withHeader("Content-Type",
            "application/json").withBody(token)));

        String entitlements = IOUtils.toString(new ClassPathResource("entitlements/entitlements.json").getInputStream
            (), defaultCharset());
        stubFor(get(urlEqualTo("/api/Entitlement/principal")).willReturn(aResponse().withStatus(200).withHeader
            ("Content-Type", "application/json").withBody(entitlements)));

        List<UserAttribute> userAttributes = subject.aggregate(input);
        assertEquals(1, userAttributes.size());

        UserAttribute userAttribute = userAttributes.get(0);
        assertEquals(EDU_PERSON_ENTITLEMENT, userAttribute.getName());

        List<String> expected = Arrays.asList(
            "urn:mace:surfnet.nl:surfmarket.nl:estudybooks:entitlement:E1",
            "urn:mace:surfnet.nl:surfmarket.nl:portal:entitlement:E1");

        assertEquals(expected, userAttribute.getValues());
    }

}