package aa.repository;

import aa.model.Account;
import aa.model.AccountType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends CrudRepository<Account, Long> {

    List<Account> findByUrnIgnoreCase(String urn);

    Optional<Account> findByUrnIgnoreCaseAndAccountType(String urn, AccountType accountType);

}
