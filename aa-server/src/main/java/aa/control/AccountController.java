package aa.control;


import aa.model.Account;
import aa.repository.AccountRepository;
import aa.shibboleth.FederatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @RequestMapping(method = RequestMethod.GET, value = "/internal/accounts")
    public List<Account> accounts(FederatedUser federatedUser) {
        return accountRepository.findByUrnIgnoreCase(federatedUser.uid);
    }


}
