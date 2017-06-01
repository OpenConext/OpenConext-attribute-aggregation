package aa.repository;

import aa.model.ServiceProvider;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface ServiceProviderRepository extends CrudRepository<ServiceProvider, Long> {

    Optional<ServiceProvider> findByEntityId(String entityId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM service_providers WHERE NOT EXISTS (SELECT service_provider_id " +
        "FROM aggregations_service_providers WHERE service_provider_id = id)", nativeQuery = true)
    int deleteOrphanedServiceProviders();


}
