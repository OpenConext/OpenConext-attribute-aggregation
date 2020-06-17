package aa.aggregators.eduid;

import aa.aggregators.ala.AbstractAlaAttributeAggregator;
import aa.aggregators.ala.AbstractAlaAttributeAggregatorTest;
import aa.model.AttributeAuthorityConfiguration;

import java.io.IOException;

public class EduIdAttributeAggregatorTest extends AbstractAlaAttributeAggregatorTest {

    public EduIdAttributeAggregatorTest() throws IOException {
    }

    @Override
    public AbstractAlaAttributeAggregator attributeAggregator(AttributeAuthorityConfiguration configuration) {
        return new EduIdAttributeAggregator(configuration);
    }

    @Override
    public String arpAggregationRequestJson() {
        return "eduid/arp_aggregation_request.json";
    }

    @Override
    public String arpSourceValue() {
        return "eduid";
    }

    @Override
    public String attributesJson() {
        return "eduid/attributes.json";
    }

}