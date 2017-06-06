package aa.repository;

import aa.AbstractIntegrationTest;
import aa.model.Account;
import aa.shibboleth.mock.MockShibbolethFilter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.*;

public class AccountRepositoryTest extends AbstractIntegrationTest{

    @Autowired
    private AccountRepository accountRepository;

    @Test
    public void findByUrnIgnoreCase() throws Exception {
        List<Account> accounts = accountRepository.findByUrnIgnoreCase(MockShibbolethFilter.SAML2_USER);
        assertEquals(1, accounts.size());
    }

}