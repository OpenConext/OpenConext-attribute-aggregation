package aa.repository;

import aa.AbstractIntegrationTest;
import aa.model.Aggregation;
import org.junit.Test;

import java.util.List;

import static aa.util.StreamUtils.listFromIterable;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
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

    @Test
    public void testGetAggregationsByServiceProviderEntityIds() throws Exception {
        List<Object[]> names = aggregationRepository.getAggregationsByServiceProviderEntityIds(
            asList("http://mock-sp", "http://unknown-sp"),
            -1L);
        assertEquals(1, names.size());
        assertEquals("test aggregation", names.get(0)[0]);
        assertEquals("http://mock-sp", names.get(0)[1]);
    }

    @Test
    public void testGetAggregationsByServiceProviderEntityIdsWithId() throws Exception {
        List<Object[]> names = aggregationRepository.getAggregationsByServiceProviderEntityIds(
            asList("http://mock-sp", "http://unknown-sp"),
            1L);
        assertEquals(0, names.size());
    }
}