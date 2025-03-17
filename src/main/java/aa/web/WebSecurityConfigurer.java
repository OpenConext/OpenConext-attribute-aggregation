package aa.web;

import aa.shibboleth.ShibbolethPreAuthenticatedProcessingFilter;
import aa.shibboleth.ShibbolethUserDetailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
    public SecurityFilterChain orcidFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        return http
                .securityMatcher("/redirect/**")
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new ShibbolethPreAuthenticatedProcessingFilter(authenticationManager),
                        AbstractPreAuthenticatedProcessingFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest()
                        .authenticated()
                )
                .build();
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

}
