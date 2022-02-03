package aa.web;

import aa.shibboleth.ShibbolethPreAuthenticatedProcessingFilter;
import aa.shibboleth.ShibbolethUserDetailService;
import aa.shibboleth.mock.MockShibbolethFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Protect endpoints for the internal API with Shibboleth AbstractPreAuthenticatedProcessingFilter.
 * <p>
 * Protect the internal endpoints for EB with basic authentication.
 * <p>
 * Do not protect public endpoints like actuator/
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfigurer {

    @Bean
    PasswordEncoder getPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        //because Autowired this will end up in the global ProviderManager
        PreAuthenticatedAuthenticationProvider authenticationProvider = new PreAuthenticatedAuthenticationProvider();
        authenticationProvider.setPreAuthenticatedUserDetailsService(new ShibbolethUserDetailService());
        auth.authenticationProvider(authenticationProvider);
    }

    @Order(1)
    @Configuration
    public static class InternalSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Autowired
        private Environment environment;

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                    .csrf().disable()
                    .requestMatchers()
                    .antMatchers("/client/**", "/redirect")
                    .and()
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                    .and()
                    .addFilterBefore(
                            new ShibbolethPreAuthenticatedProcessingFilter(authenticationManagerBean()),
                            AbstractPreAuthenticatedProcessingFilter.class
                    )
                    .addFilterBefore(new SessionAliveFilter(), ShibbolethPreAuthenticatedProcessingFilter.class)
                    .authorizeRequests()
                    .antMatchers("/client/**").hasRole("USER");

            if (environment.acceptsProfiles(Profiles.of("test"))) {
                //we can't use @Profile, because we need to add it before the real filter
                http.addFilterBefore(new MockShibbolethFilter(), ShibbolethPreAuthenticatedProcessingFilter.class);
            }
        }
    }

    @Order(2)
    @Configuration
    public static class LifeCycleSecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Value("${api.lifecycle.username}")
        private String user;

        @Value("${api.lifecycle.password}")
        private String password;

        @Override
        protected void configure(AuthenticationManagerBuilder auth) throws Exception {
            auth.inMemoryAuthentication().withUser(user).password(password).authorities("ROLE_USER");
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http
                .antMatcher("/deprovision/**")
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf().disable()
                .addFilterBefore(new BasicAuthenticationFilter(authenticationManager()), BasicAuthenticationFilter.class)
                .authorizeRequests()
                .antMatchers("/**").hasRole("USER");
        }

    }

    @Configuration
    @Order
    public static class SecurityConfigurationAdapter extends WebSecurityConfigurerAdapter {

        @Value("${security.internal_user_name}")
        private String attributeAggregationUserName;

        @Value("${security.internal_password}")
        private String attributeAggregationPassword;

        @Override
        public void configure(WebSecurity web) throws Exception {
            web.ignoring().antMatchers("/actuator/**");
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http
                .antMatcher("/**")
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .csrf()
                .disable()
                .addFilterBefore(
                    new BasicAuthenticationFilter(
                        new BasicAuthenticationManager(attributeAggregationUserName, attributeAggregationPassword)),
                    BasicAuthenticationFilter.class
                )
                .authorizeRequests()
                .antMatchers("/internal/**").hasRole("ADMIN")
                .antMatchers("/**").hasRole("USER");
        }

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
