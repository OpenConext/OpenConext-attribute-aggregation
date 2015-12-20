package aa.control;

import aa.model.ServiceProvider;
import aa.serviceregistry.ServiceRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class ServiceRegistryController {

  private final ServiceRegistry serviceRegistry;

  @Autowired
  public ServiceRegistryController(ServiceRegistry serviceRegistry) {
    this.serviceRegistry = serviceRegistry;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/internal/serviceProviders")
  public Collection<ServiceProvider> serviceProviders() {
    return serviceRegistry.serviceProviders();
  }

}
