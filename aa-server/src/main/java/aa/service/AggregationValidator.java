package aa.service;

import aa.config.AuthorityConfiguration;
import aa.model.*;
import aa.serviceregistry.ServiceRegistry;
import org.springframework.util.Assert;

import java.util.Optional;

public class AggregationValidator {

  public void validate(AuthorityConfiguration configuration, ServiceRegistry serviceRegistry, Aggregation aggregation) {
    Assert.hasText(aggregation.getName());
    Assert.notEmpty(aggregation.getServiceProviders());
    Assert.notEmpty(aggregation.getAttributes());
    aggregation.getServiceProviders().forEach(sp -> serviceProviderValid(sp, serviceRegistry));
    aggregation.getAttributes().forEach(attribute -> attributeValid(attribute, configuration));
  }

  private void serviceProviderValid(ServiceProvider serviceProvider, ServiceRegistry serviceRegistry) {
    Optional<ServiceProvider> optional = serviceRegistry.serviceProviderByEntityId(serviceProvider.getEntityId());
    if (!optional.isPresent()) {
      throw new UnknownServiceProviderException("Service Provider " + serviceProvider.getEntityId() + " is unknown");
    }
  }

  private void attributeValid(Attribute attribute, AuthorityConfiguration configuration) {
    AttributeAuthorityConfiguration authorityById = configuration.getAuthorityById(attribute.getAttributeAuthorityId());
    if (!authorityById.getAttributes().stream().anyMatch(attr -> attr.getName().equals(attribute.getName()))) {
      throw new UnknownAttributeException("Attribute with name " + attribute.getName() + " and authority " + attribute.getAttributeAuthorityId() + " is not configured " + configuration);
    }

  }

}
