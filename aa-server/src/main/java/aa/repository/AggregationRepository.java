package aa.repository;

import aa.model.Aggregation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AggregationRepository extends CrudRepository<Aggregation, Long> {

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM aggregations a WHERE LOWER(a.name) = LOWER(:name)")
    boolean existsByName(@Param("name") String name);

    @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM aggregations a WHERE LOWER(a.name) = LOWER(:name) and a.id <> :id")
    boolean existsByNameAndId(@Param("name") String name, @Param("id") Long id);

    @Query(value = "select a.name, sp.entity_id  from aggregations a inner join aggregations_service_providers asp on asp.aggregation_id = a.id " +
        "inner join service_providers sp on asp.service_provider_id = sp.id " +
        "where sp.entity_id in :serviceProviderEntityIds and a.id <> :aggregationId",
        nativeQuery = true)
    List<Object[]> getAggregationsByServiceProviderEntityIds(
        @Param("serviceProviderEntityIds") List<String> serviceProviderEntityIds,
        @Param("aggregationId") Long aggregationId);

}
