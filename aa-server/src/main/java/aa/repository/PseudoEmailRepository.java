package aa.repository;

import aa.model.PseudoEmail;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PseudoEmailRepository extends CrudRepository<PseudoEmail, Long> {

    Optional<PseudoEmail> findByEmail(String email);

}
