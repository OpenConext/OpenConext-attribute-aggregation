package aa.repository;

import aa.AbstractIntegrationTest;
import aa.model.Aggregation;
import org.junit.Test;

import java.util.List;

import static aa.util.StreamUtils.listFromIterable;

public class AggregationRepositoryTest extends AbstractIntegrationTest {

  @Test
  public void testFindAll() throws Exception {
    List<Aggregation> aggregations = listFromIterable(aggregationRepository.findAll());

    assertAggregations(aggregations);
  }

}