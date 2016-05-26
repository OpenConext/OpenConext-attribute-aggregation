package aa.config;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

public class AuthorityConfigurationTest {

  @Test(expected = IllegalArgumentException.class)
  public void testGetAuthorityById() throws Exception {
    new AuthorityConfiguration(Collections.emptyList()).getAuthorityById("nope");
  }
}