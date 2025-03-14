package aa.aggregators.sab;

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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static aa.aggregators.AttributeAggregator.EDU_PERSON_ENTITLEMENT;
import static aa.aggregators.AttributeAggregator.NAME_ID;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SabAttributeAggregatorTest {

    private SabAttributeAggregator subject;

    private List<UserAttribute> input = singletonList(new UserAttribute(NAME_ID, singletonList("urn")));

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    @Before
    public void before() {
        AttributeAuthorityConfiguration configuration = new AttributeAuthorityConfiguration("sab");
        configuration.setUser("user");
        configuration.setPassword("password");
        configuration.setEndpoint("http://localhost:8889/sab");
        configuration.setRequiredInputAttributes(singletonList(new RequiredInputAttribute(NAME_ID)));
        configuration.setValidationRegExp("^urn:mace:surfnet.nl:(surfnet\\.nl|surfmarket\\.nl|surfsara\\.nl|surf\\.nl):sab:(role|organizationCode|organizationGUID|mobile):[A-Z0-9+-]+$");
        subject = new SabAttributeAggregator(configuration);
    }

    @Test
    public void testGetRolesHappyFlow() throws Exception {
        String response = IOUtils.toString(new ClassPathResource("sab/response_success.xml").getInputStream(), Charset.defaultCharset());
        doGetRolesHappyFlow(response);
    }

    @Test
    public void testGetRolesLineBreak() throws Exception {
        String response = IOUtils.toString(new ClassPathResource("sab/response_XML_processing_instruction.xml").getInputStream(), Charset.defaultCharset());
        stubFor(post(urlEqualTo("/sab")).withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
                .willReturn(aResponse().withStatus(200).withBody(response)));
        assertEquals(6, subject.aggregate(input, Collections.emptyMap()).get(0).getValues().size());
    }

    @Test
    public void testGetRolesHappyFlowWithPrefix() throws Exception {
        String response = IOUtils.toString(new ClassPathResource("sab/response_success_with_prefix.xml").getInputStream(), Charset.defaultCharset());
        doGetRolesHappyFlow(response);
    }

    @Test
    public void testGetRolesHappyFlowWithNewPrefixes() throws Exception {
        String response = IOUtils.toString(new ClassPathResource("sab/response_success_new_prefix.xml").getInputStream(), Charset.defaultCharset());
        stubFor(post(urlEqualTo("/sab")).withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
                .willReturn(aResponse().withStatus(200).withBody(response)));
        List<UserAttribute> userAttributes = subject.aggregate(input, Collections.emptyMap());
        UserAttribute userAttribute = userAttributes.get(0);
        assertEquals(EDU_PERSON_ENTITLEMENT, userAttribute.getName());

        List<String> values = userAttribute.getValues();
        assertEquals(15, values.size());
        assertTrue(values.stream().allMatch(s -> s.startsWith("urn:mace:surfnet.nl:")));
        assertEquals(3, values.stream().filter(s -> s.equals("urn:mace:surfnet.nl:surfsara.nl:sab:3")).count());

    }

    private void doGetRolesHappyFlow(String response) {
        stubFor(post(urlEqualTo("/sab")).withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
            .willReturn(aResponse().withStatus(200).withBody(response)));
        List<UserAttribute> userAttributes = subject.aggregate(input, Collections.emptyMap());
        assertEquals(1, userAttributes.size());
        UserAttribute userAttribute = userAttributes.get(0);
        assertEquals(EDU_PERSON_ENTITLEMENT, userAttribute.getName());

        List<String> expected = Arrays.asList("Superuser", "Instellingsbevoegde", "Infraverantwoordelijke",
            "OperationeelBeheerder", "Mailverantwoordelijke", "Domeinnamenverantwoordelijke", "DNS-Beheerder",
            "AAIverantwoordelijke", "Beveiligingsverantwoordelijke").stream().sorted().collect(toList());

        assertEquals(expected.stream().map(role -> "urn:mace:surfnet.nl:surfnet.nl:sab:role:".concat(role)).collect(toList()),
            userAttribute.getValues());
    }

    @Test
    public void testGetRolesFailures() throws Exception {
        //if something goes wrong, we just don't get roles. We log all requests and responses
        for (String fileName : Arrays.asList("response_acl_blocked.xml", "response_invalid_user.xml", "response_unknown_user.xml")) {
            assertEmptyRoles(fileName);
        }
    }

    private void assertEmptyRoles(String fileName) throws IOException {
        String response = IOUtils.toString(new ClassPathResource("sab/" + fileName).getInputStream(), Charset.defaultCharset());
        stubFor(post(urlEqualTo("/sab")).withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
            .willReturn(aResponse().withStatus(200).withBody(response)));
        assertTrue(subject.aggregate(input, Collections.emptyMap()).isEmpty());
    }
}
