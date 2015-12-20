package aa.oidc;

import aa.AbstractIntegrationTest;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StreamUtils;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.nio.charset.Charset.forName;

@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=test",
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
    String json = StreamUtils.copyToString(new ClassPathResource(path).getInputStream(), forName("UTF-8"));
    oidcServerMock.stubFor(get(urlPathEqualTo("/introspect")).willReturn(
        aResponse().withStatus(200).withHeader("Content-type", "application/json").withBody(json)
    ));
  }
}
