package aa.authz;

import aa.AbstractIntegrationTest;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WebIntegrationTest(randomPort = true, value = {"spring.profiles.active=test,aa-test",
    "attribute.authorities.config.path=classpath:testAttributeAuthorities.yml",
    "authz.checkToken.endpoint.url=http://localhost:12122/oauth/check_token",
    "checkToken.cache=false"})
public abstract class AbstractAuthzIntegrationTest extends AbstractIntegrationTest {

  private static final String accessToken = UUID.randomUUID().toString();

  private int authzPort = 12122;

  protected HttpHeaders oauthHeaders;

  protected String getAccessToken() {
    return accessToken;
  }

  @Rule
  public WireMockRule authzServerMock = new WireMockRule(authzPort);

  @Before
  public void before() throws Exception {
    super.before();
    oauthHeaders = oauthHeaders(getAccessToken());

    doStubAuthzCheckToken("json/authz/check_token.success.json");

  }

  protected void doStubAuthzCheckToken(String path) throws IOException {
    InputStream inputStream = new ClassPathResource(path).getInputStream();
    String json = StreamUtils.copyToString(inputStream, Charset.forName("UTF-8"));
    authzServerMock.stubFor(post(urlMatching("/oauth/check_token")).willReturn(
        aResponse().
            withStatus(200).
            withHeader("Content-type", "application/json").
            withBody(json)
    ));
  }
}
