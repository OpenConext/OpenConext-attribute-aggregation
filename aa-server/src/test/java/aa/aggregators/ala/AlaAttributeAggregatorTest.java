package aa.aggregators.ala;

import aa.model.AttributeAuthorityConfiguration;

import java.io.IOException;

public class AlaAttributeAggregatorTest extends AbstractAlaAttributeAggregatorTest {


    public AlaAttributeAggregatorTest() throws IOException {
    }

    @Override
    public AbstractAlaAttributeAggregator attributeAggregator(AttributeAuthorityConfiguration configuration) {
        return new AlaAttributeAggregator(configuration);
    }

    @Override
    public String arpAggregationRequestJson() {
        return "ala/arp_aggregation_request.json";
    }

    @Override
    public String arpSourceValue() {
        return "ala";
    }
}