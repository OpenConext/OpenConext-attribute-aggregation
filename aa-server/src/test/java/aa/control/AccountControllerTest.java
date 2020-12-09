package aa.control;

import aa.AbstractIntegrationTest;
import aa.model.Account;
import aa.model.AccountType;
import aa.repository.AccountRepository;
import aa.shibboleth.mock.MockShibbolethFilter;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static io.restassured.RestAssured.given;
import static io.restassured.config.RedirectConfig.redirectConfig;
import static io.restassured.config.RestAssuredConfig.newConfig;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_NOT_FOUND;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"attribute_authorities_config_path=classpath:testAttributeAuthorities.yml",
                "authorization_uri=http://localhost:8889/authorize",
                "orcid.profile_redirect_uri=http://example.org",
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

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    public void connect() throws Exception {
        given()
                .config(newConfig().redirect(redirectConfig().followRedirects(false)))
                .param("redirectUrl", "https://redirect.url")
                .when()
                .get("aa/api/client/connect")
                .then()
                .statusCode(SC_MOVED_TEMPORARILY)
                .header("Location", startsWith("https://sandbox.orcid.org/oauth/authorize?"));
    }

    @Test
    public void redirect() throws Exception {
        stubFor(post(urlPathEqualTo("/token")).willReturn(aResponse()
                .withBody("{\"orcid\": \"orcid\" }")
                .withHeader("Content-Type", "application/json")));
        String encoded = passwordEncoder.encode(MockShibbolethFilter.SAML2_USER);
        given()
                .config(newConfig().redirect(redirectConfig().followRedirects(false)))
                .param("code", "123456")
                .param("state", "redirect_url=http://example.org/redirect&user_uid=" + encoded)
                .when()
                .get("aa/api/redirect")
                .then()
                .statusCode(SC_MOVED_TEMPORARILY)
                .header("Location", "http://example.org/redirect");

        List<Account> accounts = accountRepository.findByUrnIgnoreCase("saml2_user.com");
        assertEquals(1, accounts.size());

        Account account = accounts.get(0);
        assertEquals("http://orcid.org/orcid", account.getLinkedId());
        assertEquals(AccountType.ORCID, account.getAccountType());
    }

    @Test
    public void redirectWithTamperedUser() throws Exception {
        String encoded = passwordEncoder.encode("nope");
        given()
                .param("code", "123456")
                .param("state", "redirect_url=http://example.org/redirect&user_uid=" + encoded)
                .when()
                .get("aa/api/redirect")
                .then()
                .statusCode(SC_FORBIDDEN);
    }
    @Test
    public void accounts() throws Exception {
        given()
                .auth().preemptive().basic(attributeAggregationUserName, attributeAggregationPassword)
                .when()
                .get("aa/api/internal/accounts/{urn}", "saml2_user.com")
                .then()
                .statusCode(SC_OK)
                .body("id", hasItems(1))
                .body("linkedId", hasItems("http://orcid.org/0000-0002-4926-2859"));
    }

    @Test
    public void disconnect() throws Exception {
        given()
                .auth().preemptive().basic(attributeAggregationUserName, attributeAggregationPassword)
                .when()
                .delete("aa/api/internal/disconnect/{id}", 1L)
                .then()
                .statusCode(SC_OK)
                .body("status", equalTo("OK"));
        List<Account> accounts = accountRepository.findByUrnIgnoreCase("saml2_user.com");
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