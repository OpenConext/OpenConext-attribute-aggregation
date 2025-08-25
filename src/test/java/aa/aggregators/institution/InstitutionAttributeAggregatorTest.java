package aa.aggregators.institution;

import aa.model.ArpAggregationRequest;
import aa.model.ArpValue;
import aa.model.UserAttribute;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static aa.aggregators.AttributeAggregator.*;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "attribute_authorities_config_path=classpath:institution/testInstitutionAttributeAuthorities.yml")
public class InstitutionAttributeAggregatorTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8989);

    @LocalServerPort
    protected int port;

    @Before
    public void before() throws Exception {
        RestAssured.port = port;
    }

    @SneakyThrows
    @Test
    public void aggregate() {
        String eduID = UUID.randomUUID().toString();
        ArpAggregationRequest arpAggregationRequest = new ArpAggregationRequest(
                List.of(
                        new UserAttribute(SP_ENTITY_ID, List.of("https://mock-sp")),
                        new UserAttribute(EDU_ID, List.of(eduID)),
                        new UserAttribute(EDU_PERSON_PRINCIPAL_NAME, List.of("admin@example.com"))
                ),
                Map.of(EDU_PERSON_PRINCIPAL_NAME, List.of(new ArpValue("*", "institution")),
                        EMAIL, List.of(new ArpValue("*", "nope")),
                        UID, List.of(new ArpValue("*", "institution")),
                        "urn:schac:attribute-def:schacDateOfBirth", List.of(new ArpValue("*", "institution")),
                        "unknown-saml-attribute", List.of(new ArpValue("*", "institution"))));

        String response = IOUtils.toString(new ClassPathResource("institution/response.json").getInputStream(), Charset.defaultCharset());
        stubFor(get(urlPathEqualTo("/api/attributes/" + eduID))
                .withHeader("Authorization", equalTo("Basic " + encodeBase64String("api-user:secret".getBytes())))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(response)));

        List<UserAttribute> userAttributes = given()
                .auth().preemptive().basic("eb", "secret")
                .body(arpAggregationRequest)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/aa/api/internal/attribute/aggregation")
                .as(new TypeRef<>() {
                });
        assertEquals(2, userAttributes.size());
        userAttributes.sort(Comparator.comparing(UserAttribute::getName));
        UserAttribute userAttribute = userAttributes.getFirst();
        assertEquals(EDU_PERSON_PRINCIPAL_NAME, userAttribute.getName());
        assertEquals(1, userAttribute.getValues().size());
        assertEquals("johndoe@studenthartingcollege.nl", userAttribute.getValues().getFirst());

        userAttribute = userAttributes.get(1);
        assertEquals(UID, userAttribute.getName());
        assertEquals(1, userAttribute.getValues().size());
        assertEquals("jdoe123", userAttribute.getValues().getFirst());
    }

    @SneakyThrows
    @Test
    public void aggregateWithTrailingSlash() {
        String eduID = UUID.randomUUID().toString();
        ArpAggregationRequest arpAggregationRequest = new ArpAggregationRequest(
                List.of(
                        new UserAttribute(SP_ENTITY_ID, List.of("https://mock-rp")),
                        new UserAttribute(EDU_ID, List.of(eduID)),
                        new UserAttribute(EDU_PERSON_PRINCIPAL_NAME, List.of("admin@example.com"))
                ),
                Map.of(EDU_PERSON_PRINCIPAL_NAME, List.of(new ArpValue("*", "institution"))));

        String response = IOUtils.toString(new ClassPathResource("institution/response.json").getInputStream(), Charset.defaultCharset());
        stubFor(get(urlPathEqualTo("/api/attributes/" + eduID))
                .withHeader("Authorization", equalTo("Basic " + encodeBase64String("api-user:secret".getBytes())))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(response)));

        List<UserAttribute> userAttributes = given()
                .auth().preemptive().basic("eb", "secret")
                .body(arpAggregationRequest)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/aa/api/internal/attribute/aggregation")
                .as(new TypeRef<>() {
                });
        assertEquals(1, userAttributes.size());
        UserAttribute userAttribute = userAttributes.getFirst();
        assertEquals(EDU_PERSON_PRINCIPAL_NAME, userAttribute.getName());
        assertEquals(1, userAttribute.getValues().size());
        assertEquals("johndoe@studenthartingcollege.nl", userAttribute.getValues().getFirst());
    }

    @SneakyThrows
    @Test
    public void aggregateNotConfiguredServiceProvider() {
        String eduID = UUID.randomUUID().toString();
        ArpAggregationRequest arpAggregationRequest = new ArpAggregationRequest(
                List.of(
                        new UserAttribute(SP_ENTITY_ID, List.of("https://nope")),
                        new UserAttribute(EDU_ID, List.of(eduID)),
                        new UserAttribute(EDU_PERSON_PRINCIPAL_NAME, List.of("admin@example.com"))
                ),
                Map.of(EDU_PERSON_PRINCIPAL_NAME, List.of(new ArpValue("*", "institution"))));

        List<UserAttribute> userAttributes = given()
                .auth().preemptive().basic("eb", "secret")
                .body(arpAggregationRequest)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .when()
                .post("/aa/api/internal/attribute/aggregation")
                .as(new TypeRef<>() {
                });
        assertEquals(0, userAttributes.size());
    }
}