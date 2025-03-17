package aa.web;

import aa.shibboleth.ShibbolethPreAuthenticatedProcessingFilter;
import aa.shibboleth.ShibbolethUserDetailService;
import aa.shibboleth.mock.MockShibbolethFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigurer {

    @Value("${api.lifecycle.username}")
    private String lifeCycleUserName;

    @Value("${api.lifecycle.password}")
    private String lifeCyclePassword;

    @Value("${security.internal_user_name}")
    private String attributeAggregationUserName;

    @Value("${security.internal_password}")
    private String attributeAggregationPassword;

    @Bean
    PasswordEncoder getPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public AuthenticationManager configureGlobal() {
        PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(new ShibbolethUserDetailService());
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public SecurityFilterChain orcidFilterChain(HttpSecurity http,
                                                AuthenticationManager authenticationManager,
                                                Environment environment) throws Exception {
        http
                .securityMatcher("/redirect", "/client/**")
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new ShibbolethPreAuthenticatedProcessingFilter(authenticationManager),
                        AbstractPreAuthenticatedProcessingFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest()
                        .authenticated()
                );
        if (environment.acceptsProfiles(Profiles.of("test", "mock"))) {
            //we can't use @Profile, because we need to add it before the real filter
            http.addFilterBefore(new MockShibbolethFilter(), ShibbolethPreAuthenticatedProcessingFilter.class);
        }
        return http.build();
    }


    @Bean
    public SecurityFilterChain lifeCycleFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/deprovision/**")
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(
                        new BasicAuthenticationFilter(
                                new BasicAuthenticationManager(lifeCycleUserName, lifeCyclePassword)
                        ), BasicAuthenticationFilter.class
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest()
                        .authenticated()
                )
                .build();

    }

    @Bean
    public SecurityFilterChain attributeAggregationFilterChain(HttpSecurity http) throws Exception {
        return http
                .securityMatcher("/internal/**")
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new BasicAuthenticationFilter(new BasicAuthenticationManager(attributeAggregationUserName, attributeAggregationPassword)),
                        BasicAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/internal/health", "/internal/info")
                        .permitAll()
                        .anyRequest()
                        .authenticated()
                )
                .build();
    }

    @Configuration
    public class MvcConfig implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
            argumentResolvers.add(new FederatedUserHandlerMethodArgumentResolver());
        }

        @Override
        public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
            configurer.favorParameter(false);
        }

    }


}
