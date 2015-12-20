package aa.repository;

import aa.AbstractIntegrationTest;
import aa.model.Aggregation;
import aa.model.ServiceProvider;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ServiceProviderRepositoryTest extends AbstractIntegrationTest {

  @Test
  public void testFindByEntityId() throws Exception {
    Optional<ServiceProvider> sp = serviceProviderRepository.findByEntityId("http://mock-sp");
    assertTrue(sp.isPresent());

    Set<Aggregation> aggregations = sp.get().getAggregations();
    assertAggregations(aggregations);
  }

  @Test
  public void testFindByEntityIdNotExists() throws Exception {
    Optional<ServiceProvider> sp = serviceProviderRepository.findByEntityId("http://unknon-sp");
    assertFalse(sp.isPresent());
  }

}