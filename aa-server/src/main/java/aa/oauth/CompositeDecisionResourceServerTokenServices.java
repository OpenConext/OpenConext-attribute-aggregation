package aa.oauth;

import aa.util.StreamUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.List;
import java.util.Optional;

import static aa.util.StreamUtils.singletonOptionalCollector;

public class CompositeDecisionResourceServerTokenServices implements DecisionResourceServerTokenServices {

  private final List<DecisionResourceServerTokenServices> tokenServices;

  public CompositeDecisionResourceServerTokenServices(List<DecisionResourceServerTokenServices> tokenServices) {
    this.tokenServices = tokenServices;
  }

  @Override
  public boolean canHandle(String accessToken) {
    return tokenServices.stream().anyMatch(tokenServices -> tokenServices.canHandle(accessToken));
  }

  @Override
  public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {
    Optional<DecisionResourceServerTokenServices> tokenService = tokenServices.stream()
        .filter(tokenServices -> tokenServices.canHandle(accessToken)).collect(singletonOptionalCollector());
    return tokenService.orElseThrow(() -> new InvalidTokenException("Can not handle accessToken " + accessToken))
        .loadAuthentication(accessToken);
  }

  @Override
  public OAuth2AccessToken readAccessToken(String accessToken) {
    Optional<DecisionResourceServerTokenServices> tokenService = tokenServices.stream()
        .filter(tokenServices -> tokenServices.canHandle(accessToken)).collect(singletonOptionalCollector());
    return tokenService.orElseThrow(() -> new InvalidTokenException("Can not handle accessToken " + accessToken))
        .readAccessToken(accessToken);
  }
}
