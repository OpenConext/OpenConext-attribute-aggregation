package aa.serviceregistry;

import aa.model.ServiceProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;

public class ClassPathResourceServiceRegistryTest {

  private static ServiceRegistry serviceRegistry;

  @BeforeClass
  public static void beforeClass() throws IOException {
    serviceRegistry = new ClassPathResourceServiceRegistry(true);
  }

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

  @Test
  public void testAttributeAggregationRequired() {
    ServiceProvider sp = serviceRegistry.serviceProviderByEntityId("https://oidc.test.surfconext.nl").get();
    assertTrue(sp.isAttributeAggregationRequired());
  }

}