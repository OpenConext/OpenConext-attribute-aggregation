package aa.web;

import aa.shibboleth.ShibbolethPreAuthenticatedProcessingFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfigurer {

    @Value("${api.lifecycle.username}")
    private String user;

    @Value("${api.lifecycle.password}")
    private String password;

    @Value("${security.internal_user_name}")
    private String attributeAggregationUserName;

    @Value("${security.internal_password}")
    private String attributeAggregationPassword;

    @Bean
    PasswordEncoder getPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails lifeCycleUserDetails = User.withUsername(user)
                .password(getPasswordEncoder().encode(password))
                .roles("LIFE_CYLE")
                .build();
        UserDetails ebUserDetails = User.withUsername(attributeAggregationUserName)
                .password(getPasswordEncoder().encode(attributeAggregationPassword))
                .roles("ATTRIBUTE_AGGREGATION")
                .build();

        return new InMemoryUserDetailsManager(lifeCycleUserDetails, ebUserDetails);
    }


    @Bean
    public SecurityFilterChain orcidFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/redirect/**")  // Matches specific endpoints
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new ShibbolethPreAuthenticatedProcessingFilter(authenticationManagerBean()),
                        AbstractPreAuthenticatedProcessingFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").hasRole("LIFE_CYLE")
                )
                .httpBasic(Customizer.withDefaults()); // Enables Basic Authentication

        return http.build();
    }


    @Bean
    public SecurityFilterChain lifeCycleFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/deprovision/**")  // Matches specific endpoints
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new BasicAuthenticationFilter(http.getSharedObject(org.springframework.security.authentication.AuthenticationManager.class)),
                        BasicAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/**").hasRole("LIFE_CYLE")
                )
                .httpBasic(Customizer.withDefaults()); // Enables Basic Authentication

        return http.build();
    }

    @Bean
    public SecurityFilterChain attributeAggregationFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**")  // Matches specific endpoints
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .addFilterBefore(new BasicAuthenticationFilter(http.getSharedObject(org.springframework.security.authentication.AuthenticationManager.class)),
                        BasicAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/internal/health", "/internal/info")
                        .permitAll()
                        .requestMatchers("/**").hasRole("ATTRIBUTE_AGGREGATION")
                )
                .httpBasic(Customizer.withDefaults()); // Enables Basic Authentication

        return http.build();
    }

}
