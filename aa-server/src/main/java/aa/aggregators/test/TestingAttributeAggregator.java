package aa.aggregators.test;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;

import java.util.List;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

public class TestingAttributeAggregator extends AbstractAttributeAggregator {

    private final boolean useEndpoint;

    public TestingAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration,
                                      boolean useEndpoint) {
        super(attributeAuthorityConfiguration);
        this.useEndpoint = useEndpoint;
    }

    @Override
    public List<UserAttribute> aggregate(List<UserAttribute> input) {
        if (useEndpoint) {
            return getRestTemplate().exchange(new RequestEntity(HttpMethod.GET, endpoint()),
                new ParameterizedTypeReference<List<UserAttribute>>() {
                }).getBody();
        } else {
            AttributeAuthorityConfiguration configuration = getAttributeAuthorityConfiguration();
            return configuration.getAttributes().stream()
                .map(attribute -> new UserAttribute(
                    attribute.getName(),
                    singletonList("urn:x-surfnet:" + getAttributeAuthorityId() + ":test"),
                    configuration.getId()))
                .collect(toList());
        }
    }

}
