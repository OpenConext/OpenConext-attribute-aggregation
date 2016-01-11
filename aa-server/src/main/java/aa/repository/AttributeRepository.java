package aa.repository;

import aa.model.Attribute;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface AttributeRepository extends CrudRepository<Attribute, Long> {

  Optional<Attribute> findByAttributeAuthorityIdAndName(String attributeAuthorityId, String name);

}
