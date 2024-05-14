package aa.aggregators.sabrest;

import aa.model.ArpValue;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.RequiredInputAttribute;
import aa.model.UserAttribute;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static aa.aggregators.AttributeAggregator.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SabRestAttributeAggregatorTest {

    private SabRestAttributeAggregator subject;

    private final List<UserAttribute> input = List.of(
            new UserAttribute(UID, singletonList("henny")),
            new UserAttribute(SCHAC_HOME_ORGANIZATION, singletonList("surfnet.nl")));

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    @Before
    public void before() {
        AttributeAuthorityConfiguration configuration = new AttributeAuthorityConfiguration("sab");
        configuration.setUser("user");
        configuration.setPassword("password");
        configuration.setEndpoint("http://localhost:8889");
        configuration.setRequiredInputAttributes(List.of(new RequiredInputAttribute(UID), new RequiredInputAttribute(IDP_ENTITY_ID)));
        configuration.setValidationRegExp("^urn:mace:surfnet.nl:(surfnet\\.nl|surf\\.nl):sab:(role|organizationCode|organizationGUID|mobile):[A-Z0-9_+-]+$");
        subject = new SabRestAttributeAggregator(configuration);
    }

    @Test
    public void testAggregateHappyFlow() throws Exception {
        String response = IOUtils.toString(new ClassPathResource("sabrest/response_success.json").getInputStream(), Charset.defaultCharset());
        stubFor(get(urlPathEqualTo("/api/profile"))
                .withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
                .withQueryParam("uid", equalTo("henny"))
                .withQueryParam("idp", equalTo("surfnet.nl"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));
        List<UserAttribute> userAttributes = subject.aggregate(input,
                Map.of(EDU_PERSON_ENTITLEMENT, List.of(new ArpValue("*", "sabrest")),
                        SURF_AUTORISATIES, List.of(new ArpValue("*", "sabrest"))));
        assertEquals(2, userAttributes.size());
        UserAttribute userAttribute = userAttributes.get(0);
        assertEquals(EDU_PERSON_ENTITLEMENT, userAttribute.getName());
        List<String> values = List.of("urn:mace:surfnet.nl:surfnet.nl:sab:organizationCode:SURFNET",
                "urn:mace:surfnet.nl:surfnet.nl:sab:organizationGUID:ad93daef-0911-e511-80d0-005056956c1a",
                "urn:mace:surfnet.nl:surfnet.nl:sab:role:Instellingsbevoegde",
                "urn:mace:surfnet.nl:surfnet.nl:sab:role:OperationeelBeheerder",
                "urn:mace:surfnet.nl:surfnet.nl:sab:role:Superuser");
        assertEquals(values, userAttribute.getValues());

        userAttribute = userAttributes.get(1);
        assertEquals(SURF_AUTORISATIES, userAttribute.getName());
        assertEquals(values, userAttribute.getValues());

        String validationRegExp = subject.getAttributeAuthorityConfiguration().getValidationRegExp();
        Pattern pattern = Pattern.compile(validationRegExp, Pattern.CASE_INSENSITIVE);
        userAttribute.getValues().forEach(value ->
                assertTrue(value + " matches '" + validationRegExp + "'", pattern.matcher(value).matches()));
    }

    @Test
    public void testAggregateACLBlocked() throws Exception {
        String response = IOUtils.toString(new ClassPathResource("sabrest/response_acl_blocked.json").getInputStream(), Charset.defaultCharset());
        stubFor(get(urlPathEqualTo("/api/profile"))
                .withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));
        List<UserAttribute> userAttributes = subject.aggregate(input,
                Map.of(EDU_PERSON_ENTITLEMENT, List.of(new ArpValue("*", "sabrest")),
                        SURF_AUTORISATIES, List.of(new ArpValue("*", "sabrest"))));
        assertEquals(0, userAttributes.size());
    }

    @Test
    public void testAggregateMissingAttributes() throws Exception {
        String response = IOUtils.toString(new ClassPathResource("sabrest/response_missing_organisation.json").getInputStream(), Charset.defaultCharset());
        stubFor(get(urlPathEqualTo("/api/profile"))
                .withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));
        List<UserAttribute> userAttributes = subject.aggregate(input,emptyMap());
        assertEquals(0, userAttributes.size());
    }

    @Test
    public void testAggregateEmpty() throws Exception {
        String response = IOUtils.toString(new ClassPathResource("sabrest/response_empty.json").getInputStream(), Charset.defaultCharset());
        stubFor(get(urlPathEqualTo("/api/profile"))
                .withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));
        List<UserAttribute> userAttributes = subject.aggregate(input,
                Map.of(EDU_PERSON_ENTITLEMENT, List.of(new ArpValue("*", "sabrest")),
                        SURF_AUTORISATIES, List.of(new ArpValue("*", "sabrest"))));
        assertEquals(0, userAttributes.size());
    }

    @Test
    public void testAggregateEmptyProfile() throws Exception {
        String response = IOUtils.toString(new ClassPathResource("sabrest/response_empty_profile.json").getInputStream(), Charset.defaultCharset());
        stubFor(get(urlPathEqualTo("/api/profile"))
                .withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(response)));
        List<UserAttribute> userAttributes = subject.aggregate(input,
                Map.of(EDU_PERSON_ENTITLEMENT, List.of(new ArpValue("*", "sabrest")),
                        SURF_AUTORISATIES, List.of(new ArpValue("*", "sabrest"))));
        assertEquals(0, userAttributes.size());
    }
}