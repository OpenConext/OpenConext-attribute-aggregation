package aa.web;

import org.junit.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.junit.Assert.assertEquals;

public class BasicAuthenticationManagerTest {

    private final BasicAuthenticationManager subject = new BasicAuthenticationManager("user", "password");

    @Test(expected = BadCredentialsException.class)
    public void testAuthenticateInvalidUsername() throws Exception {
        subject.authenticate(new TestingAuthenticationToken("user", "nope"));
    }

    @Test(expected = UsernameNotFoundException.class)
    public void testAuthenticateInvalidPassword() throws Exception {
        subject.authenticate(new TestingAuthenticationToken("nope", "password"));
    }

    @Test
    public void happy() {
        Authentication authentication = subject.authenticate(new TestingAuthenticationToken("user", "password"));
        assertEquals("[ROLE_USER, ROLE_ADMIN]", authentication.getAuthorities().toString());
    }
}