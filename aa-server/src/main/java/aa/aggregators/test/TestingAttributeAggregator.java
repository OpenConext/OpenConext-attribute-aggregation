package aa.aggregators.test;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.ArpValue;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;

import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class TestingAttributeAggregator extends AbstractAttributeAggregator {

    public TestingAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        super(attributeAuthorityConfiguration);
    }

    @Override
    public List<UserAttribute> aggregate(List<UserAttribute> input, Map<String, List<ArpValue>> arpAttributes) {
        AttributeAuthorityConfiguration configuration = getAttributeAuthorityConfiguration();
        return configuration.getAttributes().stream()
            .map(attribute -> new UserAttribute(
                attribute.getName(),
                singletonList("urn:x-surfnet:" + getAttributeAuthorityId() + ":test"),
                configuration.getId()))
            .collect(toList());
    }
}
