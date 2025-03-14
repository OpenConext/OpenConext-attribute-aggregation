package aa.lifecycle;

import aa.model.Account;
import aa.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class UserLifeCycleController {

    private static final Logger LOG = LoggerFactory.getLogger(UserLifeCycleController.class);

    private AccountRepository accountRepository;

    @Autowired
    public UserLifeCycleController(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/deprovision/{userId:.+}")
    public LifeCycleResult preview(@PathVariable String userId, Authentication authentication) {
        LOG.info("Request for lifecycle preview for {} by {}", userId, authentication.getPrincipal());

        return doDryRun(userId, true);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/deprovision/{userId:.+}/dry-run")
    public LifeCycleResult dryRun(@PathVariable String userId, Authentication authentication) {
        LOG.info("Request for lifecycle dry-run for {} by {}", userId, authentication.getPrincipal());

        return doDryRun(userId, true);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/deprovision/{userId:.+}")
    @Transactional
    public LifeCycleResult deprovision(@PathVariable String userId, Authentication authentication) {
        LOG.info("Request for lifecycle deprovision for {} by {}", userId, authentication.getPrincipal());

        return doDryRun(userId, false);
    }

    private LifeCycleResult doDryRun(String userId, boolean dryRun) {
        LifeCycleResult result = new LifeCycleResult();
        List<Account> accounts = this.accountRepository.findByUrnIgnoreCase(userId);
        List<Attribute> attributes = accounts.stream()
            .map(account ->
                Arrays.asList(
                    new Attribute(account.getAccountType().name(), account.getLinkedId()),
                    new Attribute("urn", userId)))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        if (!dryRun) {
            accounts.forEach(account -> {
                this.accountRepository.delete(account);
                LOG.info("Deleting account of {} with {} linkedIn informationof type {}", userId,
                    account.getLinkedId(), account.getAccountType());
            });
        }
        result.setData(new ArrayList<>(attributes).stream()
            .filter(attr -> StringUtils.hasText(attr.getValue()))
            .sorted(Comparator.comparing(Attribute::getName))
            .collect(Collectors.toList()));
        return result;
    }

}
