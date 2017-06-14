package aa.control;

import aa.AbstractIntegrationTest;
import aa.model.Account;
import aa.repository.AccountRepository;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.RestAssured.given;
import static io.restassured.config.RedirectConfig.redirectConfig;
import static io.restassured.config.RestAssuredConfig.newConfig;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.junit.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"attribute_authorities_config_path=classpath:testAttributeAuthorities.yml",
        "authorization_uri=http://localhost:8889/authorize",
        "orcid.access_token_uri=http://localhost:8889/token"})
public class AccountControllerTest extends AbstractIntegrationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

    @Value("${security.internal_user_name}")
    private String attributeAggregationUserName;

    @Value("${security.internal_password}")
    private String attributeAggregationPassword;

    @Test
    public void connect() throws Exception {
        given()
            .config(newConfig().redirect(redirectConfig().followRedirects(false)))
            .param("redirectUrl", "https://redirect.url")
            .when()
            .get("aa/api/client/connect")
            .then()
            .statusCode(SC_MOVED_TEMPORARILY)
            .header("Location", "https://sandbox.orcid.org/oauth/authorize?" +
                "client_id=APP-IP57TTCD5F8BAIGS&response_type=code&scope=/authenticate&" +
                "redirect_uri=http://localhost:8080/aa/api/redirect&state=https%3A%2F%2Fredirect.url");
    }

    @Test
    public void redirect() throws Exception {
        stubFor(post(urlPathEqualTo("/token")).willReturn(aResponse()
            .withBody("{\"orcid\": \"orcid\" }")
            .withHeader("Content-Type", "application/json")));

        given()
            .config(newConfig().redirect(redirectConfig().followRedirects(false)))
            .param("code", "123456")
            .when()
            .get("aa/api/redirect")
            .then()
            .statusCode(SC_MOVED_TEMPORARILY)
            .header("Location", "http://openconext.org");
    }

    @Test
    public void accounts() throws Exception {
        given()
            .auth().preemptive().basic(attributeAggregationUserName, attributeAggregationPassword)
            .when()
            .get("aa/api/internal/accounts/{urn}", "saml2_user")
            .then()
            .statusCode(SC_OK)
            .body("id", hasItems(1))
            .body("name", hasItems("John Doe"))
            .body("linkedId", hasItems("http://orcid.org/0000-0002-4926-2859"));
    }

    @Test
    public void disconnect() throws Exception {
        given()
            .auth().preemptive().basic(attributeAggregationUserName, attributeAggregationPassword)
            .when()
            .delete("aa/api/internal/disconnect/{id}", 1L)
            .then()
            .statusCode(SC_OK);
        List<Account> accounts = accountRepository.findByUrnIgnoreCase("saml2_user");
        assertEquals(0, accounts.size());

    }

    @Test
    public void disconnectNotFound() throws Exception {
        given()
            .auth().preemptive().basic(attributeAggregationUserName, attributeAggregationPassword)
            .when()
            .delete("aa/api/internal/disconnect/{id}", 999L)
            .then()
            .statusCode(SC_NOT_FOUND);
    }
}