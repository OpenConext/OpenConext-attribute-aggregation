package aa.repository;

import aa.model.Aggregation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface AggregationRepository extends CrudRepository<Aggregation, Long> {

  @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM aggregations a WHERE LOWER(a.name) = LOWER(:name)")
  boolean existsByName(@Param("name") String name);

  @Query("SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM aggregations a WHERE LOWER(a.name) = LOWER(:name) and a.id <> :id")
  boolean existsByNameAndId(@Param("name") String name, @Param("id") Long id);

}
