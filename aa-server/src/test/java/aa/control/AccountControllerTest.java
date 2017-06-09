package aa.control;

import aa.AbstractIntegrationTest;
import aa.model.UserAttribute;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static io.restassured.RestAssured.given;
import static io.restassured.config.RedirectConfig.redirectConfig;
import static io.restassured.config.RestAssuredConfig.newConfig;
import static org.apache.http.HttpStatus.SC_MOVED_TEMPORARILY;
import static org.apache.http.HttpStatus.SC_OK;
import static org.hamcrest.Matchers.isEmptyString;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"attribute_authorities_config_path=classpath:testAttributeAuthorities.yml",
        "authorization_uri=http://localhost:8889/authorize",
        "orcid.access_token_uri=http://localhost:8889/token"})
public class AccountControllerTest extends AbstractIntegrationTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8889);

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
                "redirect_uri=http://localhost:8080/aa/api/redirect");
    }

    @Test
    public void redirect() throws Exception {
        stubFor(post(urlPathEqualTo("/token")).willReturn(aResponse()
            .withBody("{\"orcid\": \"orcid\" }")
            .withHeader("Content-Type","application/json")));

        given()
            .config(newConfig().redirect(redirectConfig().followRedirects(false)))
            .param("code", "123456")
            .when()
            .get("aa/api/redirect")
            .then()
            .statusCode(SC_MOVED_TEMPORARILY)
            .header("Location", "http://surfconext.org");
    }

}