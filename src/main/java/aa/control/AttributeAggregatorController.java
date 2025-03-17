package aa.control;

import aa.model.ArpAggregationRequest;
import aa.model.UserAttribute;
import aa.service.AttributeAggregatorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class AttributeAggregatorController {

    private final AttributeAggregatorService attributeAggregatorService;

    @Autowired
    public AttributeAggregatorController(AttributeAggregatorService attributeAggregatorService) {
        this.attributeAggregatorService = attributeAggregatorService;
    }

    @PostMapping("/internal/attribute/aggregation")
    public List<UserAttribute> arpBasedAggregation(@Valid @RequestBody ArpAggregationRequest arpAggregationRequest) {
        return attributeAggregatorService.aggregateBasedOnArp(arpAggregationRequest);
    }
}
