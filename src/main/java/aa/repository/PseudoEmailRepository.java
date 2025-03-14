package aa.repository;

import aa.model.PseudoEmail;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PseudoEmailRepository extends CrudRepository<PseudoEmail, Long> {

    Optional<PseudoEmail> findByEmailAndSpEntityId(String email, String spEntityId);

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM pseudo_emails WHERE updated < (NOW() - INTERVAL :retentionDays DAY)", nativeQuery = true)
    int deleteOlderThenRetentionDays(@Param("retentionDays") int retentionDays);

}
