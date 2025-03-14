package aa.aggregators;

import aa.config.AuthorityResolver;
import aa.model.ArpValue;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class AbstractAttributeAggregatorTest {

    private static AuthorityResolver authorityResolver;

    static {
        try {
            authorityResolver = new AuthorityResolver(new DefaultResourceLoader(),
                    "classpath:/attributeAuthorities.yml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void filterPseudoInvalidResponses() {
        AttributeAggregator pseudoEmail = attributeAggregator("pseudo_email");
        List<UserAttribute> filtered = pseudoEmail.filterInvalidResponses(
            userAttributes("john.doe@surfconext.nl", UUID.randomUUID().toString() + "@surfconext.nl", "invalid"));
        assertEquals(2, filtered.get(0).getValues().size());
    }

    @Test
    public void filterOrcidInvalidResponses() {
        AttributeAggregator orcid = attributeAggregator("orcid");
        List<UserAttribute> filtered = orcid.filterInvalidResponses(
            userAttributes("http://orcid.org/1234-0000-4321-0000","1234-0000-4321-0000", "invalid"));
        assertEquals(2, filtered.get(0).getValues().size());
    }

    @Test
    public void filterSabInvalidResponses() {
        AttributeAggregator sab = attributeAggregator("sab");
        List<UserAttribute> filtered = sab.filterInvalidResponses(
            userAttributes("urn:mace:surfnet.nl:surfnet.nl:sab:role:admin",
                "urn:mace:surfnet.nl:surfnet.nl:sab:organizationCode:nl",
                "urn:mace:surfnet.nl:surfnet.nl:sab:organizationGUID:57EAA20B-3B1E-4690-98EE-04729DB4F082",
                "urn:mace:surfnet.nl:surfnet.nl:sab:nope:valid",
                "invalid"));
        assertEquals(3, filtered.get(0).getValues().size());
    }

    @Test
    public void filterVootInvalidResponses() {
        AttributeAggregator voot = attributeAggregator("voot");
        List<UserAttribute> filtered = voot.filterInvalidResponses(
            userAttributes("urn:collab:group:test1", "invalid"));
        assertEquals(1, filtered.get(0).getValues().size());
    }

    @Test
    public void filterEntitlementsInvalidResponses() {
        AttributeAggregator entitlements = attributeAggregator("surfmarket_entitlements");
        List<UserAttribute> filtered = entitlements.filterInvalidResponses(
            userAttributes("urn:mace:surfnet.nl:surfmarket.nl:admin12@#", "invalid"));
        assertEquals(1, filtered.get(0).getValues().size());
    }

    private AttributeAggregator attributeAggregator(String key) {
        AttributeAuthorityConfiguration attributeAuthorityConfiguration =
            authorityResolver.getConfiguration().getAuthorityById(key);
        return new AbstractAttributeAggregator(attributeAuthorityConfiguration) {
            @Override
            public List<UserAttribute> aggregate(List<UserAttribute> input, Map<String, List<ArpValue>> arpAttributes) {
                throw new IllegalArgumentException();
            }
        };
    }

    private List<UserAttribute> userAttributes(String... values) {
        return Collections.singletonList(new UserAttribute("name", Arrays.asList(values), "source"));
    }

}