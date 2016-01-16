package aa.repository;

import aa.AbstractIntegrationTest;
import aa.model.Aggregation;
import org.junit.Test;

import java.util.List;

import static aa.util.StreamUtils.listFromIterable;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AggregationRepositoryTest extends AbstractIntegrationTest {

  @Test
  public void testFindAll() throws Exception {
    List<Aggregation> aggregations = listFromIterable(aggregationRepository.findAll());

    assertAggregations(aggregations);
  }

  @Test
  public void existsByName() throws Exception {
    assertTrue(aggregationRepository.existsByName("TEST AGGREGATION"));
    assertFalse(aggregationRepository.existsByName("nope"));
  }

  @Test
  public void existsByNameAndId() throws Exception {
    assertFalse(aggregationRepository.existsByNameAndId("TEST AGGREGATION", 1L));
    assertTrue(aggregationRepository.existsByNameAndId("TEST AGGREGATION", 2L));
  }
}