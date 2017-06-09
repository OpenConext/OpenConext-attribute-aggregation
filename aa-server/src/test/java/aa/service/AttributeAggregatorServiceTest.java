package aa.service;

import aa.aggregators.AttributeAggregator;
import aa.aggregators.test.TestingAttributeAggregator;
import aa.cache.SimpleInMemoryUserAttributeCache;
import aa.config.AuthorityConfiguration;
import aa.model.ArpAggregationRequest;
import aa.model.ArpValue;
import aa.model.Attribute;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.RequiredInputAttribute;
import aa.model.UserAttribute;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static aa.aggregators.AttributeAggregator.NAME_ID;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.Assert.assertEquals;

public class AttributeAggregatorServiceTest {

    private SimpleInMemoryUserAttributeCache cache = new SimpleInMemoryUserAttributeCache(1_000_000_000, 10000);

    @Test
    public void aggregateBasedOnArpWildCard() throws Exception {
        List<UserAttribute> userAttributes = doAggregate("*");
        assertEquals(singletonList(new UserAttribute("name", singletonList("urn:x-surfnet:test:test"), "test")), userAttributes);

        //hit cache
        userAttributes = doAggregate("*", true);
        assertEquals(singletonList(new UserAttribute("name", singletonList("urn:x-surfnet:test:test"), "test")), userAttributes);
    }

    @Test
    public void aggregateBasedOnArpPrefix() throws Exception {
        List<UserAttribute> userAttributes = doAggregate("urn:x-surfnet*");
        assertEquals(singletonList(new UserAttribute("name", singletonList("urn:x-surfnet:test:test"), "test")), userAttributes);
    }

    @Test
    public void aggregateBasedOnArpError() throws Exception {
        List<UserAttribute> userAttributes = doAggregate("urn:x-surfnet*", true);
        assertEquals(0, userAttributes.size());
    }

    @Test
    public void aggregateBasedOnArpExactMatch() throws Exception {
        List<UserAttribute> userAttributes = doAggregate("urn:x-surfnet:test:test");
        assertEquals(singletonList(new UserAttribute("name", singletonList("urn:x-surfnet:test:test"), "test")), userAttributes);
    }

    @Test
    public void aggregateBasedOnArpNoMatch() throws Exception {
        List<UserAttribute> userAttributes = doAggregate("nope");
        assertEquals(0, userAttributes.size());
    }

    private List<UserAttribute> doAggregate(String arpValue) {
        return doAggregate(arpValue, false);
    }

    private List<UserAttribute> doAggregate(String arpValue, boolean throwError) {
        Attribute attribute = new Attribute();
        attribute.setName("name");
        AttributeAuthorityConfiguration attributeAuthorityConfiguration =
            new AttributeAuthorityConfiguration("test", singletonList(attribute));
        attributeAuthorityConfiguration.setRequiredInputAttributes(Collections.singletonList(new RequiredInputAttribute(NAME_ID)));

        List<AttributeAuthorityConfiguration> authorities = singletonList(attributeAuthorityConfiguration);
        AuthorityConfiguration authorityConfiguration = new AuthorityConfiguration(authorities);
        TestingAttributeAggregator attributeAggregator = new TestingAttributeAggregator(attributeAuthorityConfiguration) {
            @Override
            public List<UserAttribute> aggregate(List<UserAttribute> input) {
                if (throwError) {
                    throw new RuntimeException("oeps");
                }
                return super.aggregate(input);
            }
        };
        AttributeAggregatorService subject = new AttributeAggregatorService(
            singletonList(attributeAggregator),
            authorityConfiguration,
            cache
        );

        return subject.aggregateBasedOnArp(new ArpAggregationRequest(
            singletonList(new UserAttribute(NAME_ID, singletonList("urn"))),
            singletonMap("name", singletonList(new ArpValue(arpValue, "test")))
        ));
    }

}