package aa.control;

import aa.model.ArpAggregationRequest;
import aa.model.ServiceProvider;
import aa.model.UserAttribute;
import aa.model.UserAttributes;
import aa.repository.ServiceProviderRepository;
import aa.service.AttributeAggregatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class AttributeAggregatorController {

    private final static Logger LOG = LoggerFactory.getLogger(AttributeAggregatorController.class);

    private final ServiceProviderRepository serviceProviderRepository;
    private final AttributeAggregatorService attributeAggregatorService;

    @Autowired
    public AttributeAggregatorController(ServiceProviderRepository serviceProviderRepository,
                                         AttributeAggregatorService attributeAggregatorService) {
        this.serviceProviderRepository = serviceProviderRepository;
        this.attributeAggregatorService = attributeAggregatorService;
    }

    @RequestMapping(method = RequestMethod.POST, value = {"/attribute/aggregate", "/internal/attribute/aggregate"})
    public List<UserAttribute> attributeAggregate(@RequestBody UserAttributes input) {
        Optional<ServiceProvider> sp = serviceProviderRepository.findByEntityId(input.getServiceProviderEntityId());
        if (!sp.isPresent()) {
            LOG.debug("ServiceProvider {} has no attribute aggregations configured. Returning empty list.", input.getServiceProviderEntityId());
            return Collections.emptyList();
        }
        return attributeAggregatorService.aggregate(sp.get(), input.getAttributes());
    }

    @RequestMapping(method = RequestMethod.POST, value = {"/attribute/aggregateNoServiceCheck", "/internal/attribute/aggregateNoServiceCheck"})
    public List<UserAttribute> attributeAggregateNoServiceCheck(@RequestBody UserAttributes input) {
        return attributeAggregatorService.aggregateNoServiceCheck(input.getAttributes());
    }

    @RequestMapping(method = RequestMethod.POST, value = {"/attribute/arpBasedAggregation", "/internal/attribute/arpBasedAggregation"})
    public List<UserAttribute> arpBasedAggregation(@Valid @RequestBody ArpAggregationRequest arpAggregationRequest) {
        return attributeAggregatorService.aggregateBasedOnArp(arpAggregationRequest);
    }
}
