package aa.aggregators.orcid;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.Account;
import aa.model.AccountType;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import aa.repository.AccountRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;

public class OrcidAttributeAggregator extends AbstractAttributeAggregator {

    private AccountRepository accountRepository;

    public OrcidAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration,
                                    AccountRepository accountRepository) {
        super(attributeAuthorityConfiguration);
        this.accountRepository = accountRepository;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<UserAttribute> aggregate(List<UserAttribute> input) {
        String urn = getUserAttributeSingleValue(input, NAME_ID);
        Optional<Account> accountOptional = accountRepository.findByUrnIgnoreCaseAndAccountType(urn, AccountType.ORCID);

        LOG.debug("Retrieved ORCID for urn: {} and result {}", urn, accountOptional);

        List<String> orcids = accountOptional.map(account -> singletonList(account.getLinkedId())).orElse(Collections.emptyList());

        return mapValuesToUserAttribute(ORCID, orcids);
    }

}
