package aa.serviceregistry;

import aa.model.ServiceProvider;

import java.util.Collection;
import java.util.Optional;

public interface ServiceRegistry {

  Collection<ServiceProvider> serviceProviders();

  Optional<ServiceProvider> serviceProviderByEntityId(String entityId);
}
