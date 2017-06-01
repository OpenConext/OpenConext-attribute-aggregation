package aa.config;

import org.junit.Test;

import java.util.Collections;

public class AuthorityConfigurationTest {

    @Test(expected = IllegalArgumentException.class)
    public void testGetAuthorityById() throws Exception {
        new AuthorityConfiguration(Collections.emptyList()).getAuthorityById("nope");
    }
}