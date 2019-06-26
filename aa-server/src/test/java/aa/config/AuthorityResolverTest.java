package aa.config;

import aa.model.Attribute;
import aa.model.AttributeAuthorityConfiguration;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AuthorityResolverTest {

    @Test
    public void testGetTestConfiguration() throws Exception {
        AuthorityResolver subject = new AuthorityResolver(new DefaultResourceLoader(), "classpath:/testAttributeAuthorities.yml");
        testConfiguration(subject, 3);
    }

    @Test
    public void testGetConfiguration() throws Exception {
        AuthorityResolver subject = new AuthorityResolver(new DefaultResourceLoader(), "classpath:/attributeAuthorities.yml");
        testConfiguration(subject, 5);
    }

    private void testConfiguration(AuthorityResolver subject, int expectedAuthorities) {
        AuthorityConfiguration configuration = subject.getConfiguration();
        Collection<AttributeAuthorityConfiguration> authorities = configuration.getAuthorities();

        assertEquals(expectedAuthorities, authorities.size());

        authorities.forEach(this::assertAuthority);
    }

    private void assertAuthority(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        attributeAuthorityConfiguration.getAttributes().forEach(attribute -> this.assertAttribute(attributeAuthorityConfiguration, attribute));
        assertTrue(attributeAuthorityConfiguration.getRequiredInputAttributes().size() > 0);
    }

    private void assertAttribute(AttributeAuthorityConfiguration attributeAuthorityConfiguration, Attribute attribute) {
        assertEquals(attributeAuthorityConfiguration.getId(), attribute.getAttributeAuthorityId());
    }
}