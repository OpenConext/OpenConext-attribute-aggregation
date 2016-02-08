package aa.oauth;

import aa.oidc.OidcRemoteTokenServices;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.client.RestTemplate;

import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CachedRemoteTokenServicesTest {

  private static ObjectMapper objectMapper = new ObjectMapper();

  @Test
  @SuppressWarnings("unchecked")
  public void testLoadAuthentication() throws Exception {
    OidcRemoteTokenServices tokenServices = new OidcRemoteTokenServices("http://dummy", "clientId", "secret");
    RestTemplate restTemplate = mock(RestTemplate.class);
    tokenServices.setRestTemplate(restTemplate);

    CachedRemoteTokenServices remoteTokenServices = new CachedRemoteTokenServices(tokenServices, 200, 150);

    InputStream inputStream = new ClassPathResource("json/oidc/introspect.success.json").getInputStream();

    Map<String, Object> map = objectMapper.readValue(inputStream, Map.class);
    ResponseEntity<Map> response = new ResponseEntity(map, HttpStatus.OK);

    when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(Map.class))).thenReturn(response);

    OAuth2Authentication authentication = remoteTokenServices.loadAuthentication("access_token");
    map.put("unspecified_id", "someone_else");

    OAuth2Authentication cachedAuthentication = remoteTokenServices.loadAuthentication("access_token");
    // we expect to hit the cache
    assertEquals(authentication, cachedAuthentication);

    Thread.sleep(500);

    OAuth2Authentication newAuthentication = remoteTokenServices.loadAuthentication("access_token");

    //cache is cleaned
    assertNotEquals(authentication, newAuthentication);
  }

  @Test
  public void testDelegateCalls() {
    DecisionResourceServerTokenServices tokenServices = mock(DecisionResourceServerTokenServices.class);
    CachedRemoteTokenServices subject = new CachedRemoteTokenServices(tokenServices, 1, 1000);

    String accessToken = "accessToken";
    DefaultOAuth2AccessToken token = new DefaultOAuth2AccessToken(accessToken);
    when(tokenServices.canHandle(accessToken)).thenReturn(false);
    when(tokenServices.readAccessToken(accessToken)).thenReturn(token);

    assertFalse(subject.canHandle(accessToken));
    assertEquals(token, subject.readAccessToken(accessToken));
  }
}
