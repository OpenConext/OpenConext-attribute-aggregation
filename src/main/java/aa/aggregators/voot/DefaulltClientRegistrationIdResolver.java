package aa.aggregators.voot;

import org.springframework.http.HttpRequest;
import org.springframework.security.oauth2.client.web.client.OAuth2ClientHttpRequestInterceptor;

import static aa.aggregators.voot.VootAttributeAggregator.REGISTRATION_ID;

public class DefaulltClientRegistrationIdResolver implements OAuth2ClientHttpRequestInterceptor.ClientRegistrationIdResolver {

    @Override
    public String resolve(HttpRequest request) {
        return REGISTRATION_ID;
    }
}
