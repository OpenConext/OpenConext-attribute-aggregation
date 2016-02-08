package aa.serviceregistry;

import aa.model.ServiceProvider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ClassPathResourceServiceRegistryTest {

  private static ServiceRegistry serviceRegistry = new ClassPathResourceServiceRegistry(true);

  @Test
  public void testServiceProviders() throws Exception {
    assertEquals(4, serviceRegistry.serviceProviders().size());
  }

  @Test
  public void testServiceProviderByEntityId() throws Exception {
    ServiceProvider sp = serviceRegistry.serviceProviderByEntityId("https://oidc.localhost.surfconext.nl").get();
    assertEquals("OIDC localhost name", sp.getName());
    assertEquals("OIDC localhost description", sp.getDescription());
  }

  @Test
  public void testServiceProviderByEntityIdIllegalArgument() throws Exception {
    assertFalse(serviceRegistry.serviceProviderByEntityId("https://unknown/sp").isPresent());
  }

}