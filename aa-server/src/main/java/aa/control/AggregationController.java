package aa.control;

import aa.config.AuthorityConfiguration;
import aa.config.AuthorityResolver;
import aa.model.Aggregation;
import aa.model.AggregationNotFoundException;
import aa.model.Attribute;
import aa.model.ServiceProvider;
import aa.repository.AggregationRepository;
import aa.repository.AttributeRepository;
import aa.repository.ServiceProviderRepository;
import aa.service.AggregationValidator;
import aa.serviceregistry.ServiceRegistry;
import aa.shibboleth.FederatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static aa.util.StreamUtils.listFromIterable;
import static java.util.stream.Collectors.toSet;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class AggregationController {

  private final static Logger LOG = LoggerFactory.getLogger(AggregationController.class);

  private final AggregationValidator aggregationValidator = new AggregationValidator();

  private final ServiceProviderRepository serviceProviderRepository;
  private final AggregationRepository aggregationRepository;
  private final AttributeRepository attributeRepository;
  private final AuthorityConfiguration configuration;
  private final ServiceRegistry serviceRegistry;

  @Autowired
  public AggregationController(ServiceProviderRepository serviceProviderRepository,
                               AggregationRepository aggregationRepository,
                               AttributeRepository attributeRepository,
                               AuthorityResolver authorityResolver,
                               ServiceRegistry serviceRegistry) {
    this.serviceProviderRepository = serviceProviderRepository;
    this.aggregationRepository = aggregationRepository;
    this.attributeRepository = attributeRepository;
    this.configuration = authorityResolver.getConfiguration();
    this.serviceRegistry = serviceRegistry;
  }

  @RequestMapping(method = POST, value = "/internal/aggregation")
  public Aggregation saveAggregation(@RequestBody Aggregation aggregation) {
    aggregationValidator.validate(configuration, serviceRegistry, aggregation);

    aggregation.setServiceProviders(aggregation.getServiceProviders().stream().map(this::serviceProviderOrExists).collect(toSet()));
    aggregation.setAttributes(aggregation.getAttributes().stream().map(this::attributeOrExists).collect(Collectors.toSet()));

    FederatedUser federatedUser = federatedUser();
    aggregation.setUserDisplayName(federatedUser.getDisplayName());
    aggregation.setUserIdentifier(federatedUser.getUid());

    Aggregation saved = this.aggregationRepository.save(aggregation);

    LOG.debug("Saved aggregation {}", saved);
    return saved;

  }

  @RequestMapping(method = GET, value = "/internal/aggregation/{id}")
  public Aggregation getAggregation(@PathVariable Long id) {
    Aggregation aggregation = this.aggregationRepository.findOne(id);
    if (aggregation == null) {
      throw new AggregationNotFoundException("Aggregation with id " + id + " not found");
    }
    aggregation.getServiceProviders().forEach(this::addMetaDataInformation);

    LOG.debug("Returning one aggregation {}", aggregation);

    return aggregation;
  }

  @RequestMapping(method = PUT, value = "/internal/aggregation")
  public Aggregation updateAggregation(@RequestBody Aggregation aggregation) {
    Aggregation saved = this.saveAggregation(aggregation);
    this.serviceProviderRepository.deleteOrphanedServiceProviders();
    return saved;
  }

  @RequestMapping(method = DELETE, value = "/internal/aggregation/{id}")
  public Aggregation deleteAggregation(@PathVariable Long id) {
    Aggregation aggregation = aggregationRepository.findOne(id);
    this.aggregationRepository.delete(aggregation);
    LOG.debug("Deleted Aggregation with ID {}", id);
    int deleted = this.serviceProviderRepository.deleteOrphanedServiceProviders();
    LOG.debug("Deleted {} orphaned ServiceProviders", deleted);
    return aggregation;
  }

  @RequestMapping(method = GET, value = "/internal/aggregations")
  public List<Aggregation> aggregations() {
    List<Aggregation> aggregations = listFromIterable(this.aggregationRepository.findAll());
    aggregations.forEach(aggregation -> aggregation.getServiceProviders().forEach(this::addMetaDataInformation));

    LOG.debug("Returning all aggregations {}", aggregations);

    return aggregations;
  }

  @RequestMapping(method = GET, value = "/internal/aggregationExistsByName")
  public boolean aggregationExistsByName(@RequestParam("name") String name, @RequestParam(value = "id", required = false) Long id) {
    return id == null ? aggregationRepository.existsByName(name) : aggregationRepository.existsByNameAndId(name, id);
  }

  private void addMetaDataInformation(ServiceProvider serviceProvider) {
    Optional<ServiceProvider> optional = serviceRegistry.serviceProviderByEntityId(serviceProvider.getEntityId());
    if (optional.isPresent()) {
      ServiceProvider sp = optional.get();
      serviceProvider.setName(sp.getName());
      serviceProvider.setDescription(sp.getDescription());
    }
  }

  private ServiceProvider serviceProviderOrExists(ServiceProvider serviceProvider) {
    return serviceProviderRepository.findByEntityId(serviceProvider.getEntityId()).orElse(serviceProvider);
  }

  private Attribute attributeOrExists(Attribute attribute) {
    return attributeRepository.findByAttributeAuthorityIdAndName(attribute.getAttributeAuthorityId(), attribute.getName()).orElse(attribute);
  }

  private FederatedUser federatedUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    Assert.isInstanceOf(FederatedUser.class, principal);
    return (FederatedUser) principal;
  }


}
