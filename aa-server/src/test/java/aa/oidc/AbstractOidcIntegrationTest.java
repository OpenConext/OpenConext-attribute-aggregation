package aa.oidc;

import aa.AbstractIntegrationTest;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.nio.charset.Charset;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    value = {"spring.profiles.active=aa-test",
    "attribute.authorities.config.path=classpath:testAttributeAuthorities.yml",
    "oidc.checkToken.endpoint.url=http://localhost:12121/introspect",
    "checkToken.cache=false"})
public abstract class AbstractOidcIntegrationTest extends AbstractIntegrationTest {

  private int oidcPort = 12121;

  protected HttpHeaders oauthHeaders = oauthHeaders(getAccessToken());

  protected String getAccessToken() {
    return "TOKEN_VALUE";
  }

  @Rule
  public WireMockRule oidcServerMock = new WireMockRule(oidcPort);

  @Before
  public void before() throws Exception {
    super.before();
    stubOidcCheckTokenUser("json/oidc/introspect.success.json");
  }

  @Override
  protected boolean isBasicAuthenticated() {
    return false;
  }

  protected void stubOidcCheckTokenUser(String path) throws IOException {
    String json = IOUtils.toString(new ClassPathResource(path).getInputStream(), Charset.defaultCharset());
    oidcServerMock.stubFor(get(urlPathEqualTo("/introspect")).willReturn(
        aResponse().withStatus(200).withHeader("Content-type", "application/json").withBody(json)
    ));
  }
}
