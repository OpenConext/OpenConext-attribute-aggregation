package aa.control;


import aa.model.Account;
import aa.model.AccountType;
import aa.model.ResourceNotFoundException;
import aa.repository.AccountRepository;
import aa.shibboleth.FederatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class AccountController {

    private final static Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private AccountRepository accountRepository;

    @Value("${orcid.authorization_uri}")
    private String orcidAuthorizationUri;

    @Value("${orcid.access_token_uri}")
    private String orcidAccessTokenUri;

    @Value("${orcid.client_id}")
    private String orcidClientId;

    @Value("${orcid.client_secret}")
    private String orcidClientSecret;

    @Value("${orcid.redirect_uri}")
    private String orcidRedirectUri;

    private RestTemplate restTemplate = new RestTemplate();

    private HttpHeaders httpHeaders = new HttpHeaders();

    public AccountController() {
        this.httpHeaders.add(HttpHeaders.ACCEPT, "application/json");
    }

    @GetMapping("/client/accounts")
    public List<Account> accounts(FederatedUser federatedUser) {
        List<Account> accounts = accountRepository.findByUrnIgnoreCase(federatedUser.uid);
        LOG.debug("Accounts {} for {}", accounts, federatedUser.uid);
        return accounts;
    }

    @GetMapping("/client/connect")
    public void connect(FederatedUser federatedUser, HttpServletResponse response) throws IOException {
        LOG.debug("Starting ORCID connection linking for {}", federatedUser.uid);
        String uri = String.format("%s?client_id=%s&response_type=code&scope=/authenticate&redirect_uri=%s",
            orcidAuthorizationUri, orcidClientId, orcidRedirectUri);
        response.sendRedirect(uri);
    }

    @GetMapping("/redirect")
    public void redirect(FederatedUser federatedUser, @RequestParam("code") String code) {
        LOG.debug("Redirect from ORCID for {} with code {}", federatedUser.uid, code);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", orcidClientId);
        map.add("client_secret", orcidClientSecret);
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("redirect_uri", orcidRedirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, httpHeaders);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(orcidAccessTokenUri, HttpMethod.POST, request, Map.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException(String.format("Unable to exchange code for ORCID id {}", responseEntity));
        }
        String orcid = String.class.cast(responseEntity.getBody().get("orcid"));

        Optional<Account> accountOptional = accountRepository.findByUrnIgnoreCaseAndAccountType(federatedUser.uid, AccountType.ORCID);
        Account account = accountOptional.orElseGet(() -> new Account(federatedUser.uid, federatedUser.displayName, AccountType.ORCID));
        account.setLinkedId(orcid);

        LOG.debug("Saving ORCID linked account {}", account);

        accountRepository.save(account);
    }

    @GetMapping("/internal/accounts/{urn}")
    public List<Account> accounts(@PathVariable("urn") String urn) {
        List<Account> accounts = accountRepository.findByUrnIgnoreCase(urn);
        LOG.debug("Accounts {} for {}", accounts, urn);
        return accounts;
    }

    @PutMapping("/internal/disconnect/{id}")
    public void disconnect(@PathVariable("id") Long id) {
        Account account = accountRepository.findOne(id);
        if (account == null) {
            throw new ResourceNotFoundException(String.format("Account %s not found", id));
        }
        LOG.debug("Deleting account {}", account);
        accountRepository.delete(account);
    }


}
