package aa.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;

public class AttributeAuthorityConfigurationTest {

  @Test
  public void testEquals() throws Exception {
    AttributeAuthorityConfiguration authority = new AttributeAuthorityConfiguration("id1");
    AttributeAuthorityConfiguration other = new AttributeAuthorityConfiguration("id1");

    assertEquals(authority, other);

    assertEquals(1, new HashSet(Arrays.asList(authority, other)).size());
  }
}