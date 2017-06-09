package aa.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AttributeAuthorityConfigurationTest {

    @Test
    public void testEquals() throws Exception {
        AttributeAuthorityConfiguration authority = new AttributeAuthorityConfiguration("id1");
        AttributeAuthorityConfiguration other = new AttributeAuthorityConfiguration("id1");

        assertTrue(authority.equals(authority));
        assertFalse(authority.equals(new Object()));
        assertEquals(authority, other);

        assertEquals(1, new HashSet<>(Arrays.asList(authority, other)).size());
    }
}