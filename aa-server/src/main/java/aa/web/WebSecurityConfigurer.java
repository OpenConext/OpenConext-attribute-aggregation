package aa.web;

import aa.authz.AuthzResourceServerTokenServices;
import aa.authz.AuthzSchacHomeAwareUserAuthenticationConverter;
import aa.oauth.CachedRemoteTokenServices;
import aa.oauth.CompositeDecisionResourceServerTokenServices;
import aa.oauth.DecisionResourceServerTokenServices;
import aa.oidc.OidcRemoteTokenServices;
import aa.shibboleth.ShibbolethPreAuthenticatedProcessingFilter;
import aa.shibboleth.ShibbolethUserDetailService;
import aa.shibboleth.mock.MockShibbolethFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.authentication.BearerTokenExtractor;
import org.springframework.security.oauth2.provider.authentication.TokenExtractor;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableResourceServer
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter implements ResourceServerConfigurer {

  @Value("${attribute.aggregation.user.name}")
  private String attributeAggregationUserName;

  @Value("${attribute.aggregation.user.password}")
  private String attributeAggregationPassword;

  @Value("${authz.checkToken.endpoint.url}")
  private String authzCheckTokenEndpointUrl;

  @Value("${authz.checkToken.clientId}")
  private String authzCheckTokenClientId;

  @Value("${authz.checkToken.secret}")
  private String authzCheckTokenSecret;

  @Value("${oidc.checkToken.endpoint.url}")
  private String oidcCheckTokenEndpointUrl;

  @Value("${oidc.checkToken.clientId}")
  private String oidcCheckTokenClientId;

  @Value("${oidc.checkToken.secret}")
  private String oidcCheckTokenSecret;

  @Value("${checkToken.cache}")
  private boolean checkTokenCache;

  @Value("${checkToken.cache.duration.milliSeconds}")
  private int checkTokenCacheDurationMilliseconds;

  @Autowired
  private Environment environment;

  private boolean configured;

  @Override
  public void configure(WebSecurity web) throws Exception {
    web.ignoring().antMatchers("/health/**");
  }

  /*
   * This needs to stay in sync with the apache configuration for Attribute Aggregation
   */
  public static final String NON_SHIBBOLETH_PROTECTED_METHODS =
      "^(?!/attribute/aggregate|/health|/v2/ServiceProviderConfig|/v2/ResourceType|/v2/Schema|/v2/Me).*$";


  /**
   * Protect endpoints for the internal API with Shibboleth AbstractPreAuthenticatedProcessingFilter.
   * <p>
   * Protect the internal endpoint for EB and Dashbaord with basic authentication.
   * <p>
   * Protect all other endpoints - except the public ones - with OAuth2 with support for both Authz and OIDC.
   * <p>
   * Do not protect public endpoints like /health, /info and /ServiceProviderConfig
   * <p>
   * Protect the /Me endpoint with an OAuth2 access_token associated with an User authentication
   * <p>
   * Protect the /Schema endpoint with an OAuth2 client credentials access_token
   */
  @Override
  public void configure(HttpSecurity http) throws Exception {
    if (this.configured) {
      //we must ensure we don't get configured twice because of the combination WebSecurityConfigurerAdapter and ResourceServerConfigurer
      return;
    }
    this.configured = true;
    http
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
        .and()
        .csrf()
        .requireCsrfProtectionMatcher(new CsrfProtectionMatcher())
        .and()
        .addFilterAfter(new CsrfTokenResponseHeaderBindingFilter(), CsrfFilter.class)
        .addFilterBefore(new SessionAliveFilter(), CsrfFilter.class)
        .addFilterBefore(
            new BasicAuthenticationFilter(
                new BasicAuthenticationManager(attributeAggregationUserName, attributeAggregationPassword)),
            AbstractPreAuthenticatedProcessingFilter.class
        )
        //ensure we don't create a shib principal for methods that are not protected by shib in the apache conf
        .addFilterAfter(new RegExpRequestMatcherFilter(
                new ShibbolethPreAuthenticatedProcessingFilter(authenticationManagerBean()),
                NON_SHIBBOLETH_PROTECTED_METHODS),
            BasicAuthenticationFilter.class
        )
        .authorizeRequests()
        .antMatchers("/v2/ResourceType", "/v2/Me", "/v2/Schema").access("#oauth2.hasScope('attribute-aggregation')")
        .antMatchers("/v2/query").access("#oauth2.hasScope('saml-attribute-query')")
        .antMatchers("/attribute/**").hasRole("ADMIN")
        .antMatchers("/internal/**").hasRole("ADMIN")
        .antMatchers("/health/**", "/v2/ServiceProviderConfig").permitAll()
        .antMatchers("/**").hasRole("USER");

    if (environment.acceptsProfiles("no-csrf")) {
      http.csrf().disable();
    }
    if (environment.acceptsProfiles("dev", "no-csrf")) {
      //we can't use @Profile, because we need to add it before the real filter
      http.addFilterBefore(new MockShibbolethFilter(), ShibbolethPreAuthenticatedProcessingFilter.class);
    }
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
    authenticationProvider.setPreAuthenticatedUserDetailsService(new ShibbolethUserDetailService());
    auth.authenticationProvider(authenticationProvider);
  }

  @Override
  public void configure(ResourceServerSecurityConfigurer resources) {
    //if we run stateless, then only oauth2 is allowed to populate the security context and this is not what we want
    resources.resourceId("attribute-aggregator").stateless(false).tokenServices(resourceServerTokenServices()).tokenExtractor(tokenExtractor());
  }

  private DecisionResourceServerTokenServices resourceServerTokenServices() {
    CompositeDecisionResourceServerTokenServices tokenServices = new CompositeDecisionResourceServerTokenServices(
        Arrays.asList(oidcResourceServerTokenServices(), authzResourceServerTokenServices())
    );
    return checkTokenCache ?
        new CachedRemoteTokenServices(tokenServices, checkTokenCacheDurationMilliseconds, checkTokenCacheDurationMilliseconds) :
        tokenServices;
  }

  private DecisionResourceServerTokenServices oidcResourceServerTokenServices() {
    return new OidcRemoteTokenServices(oidcCheckTokenEndpointUrl, oidcCheckTokenClientId, oidcCheckTokenSecret);
  }

  private DecisionResourceServerTokenServices authzResourceServerTokenServices() {
    final DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
    accessTokenConverter.setUserTokenConverter(new AuthzSchacHomeAwareUserAuthenticationConverter());
    return new AuthzResourceServerTokenServices(authzCheckTokenClientId, authzCheckTokenSecret, authzCheckTokenEndpointUrl, accessTokenConverter);
  }

  /*
   * Explicitly deny other means of supplying oauth token than "bearer"
   */
  private TokenExtractor tokenExtractor() {
    return new BearerTokenExtractor() {
      protected String extractToken(HttpServletRequest request) {
        // only check the header...
        return extractHeaderToken(request);
      }
    };
  }

}
