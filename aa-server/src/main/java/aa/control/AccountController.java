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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class AccountController {

    private final static Logger LOG = LoggerFactory.getLogger(AccountController.class);
    private static final String CLIENT_REDIRECT_URL = "client_redirect_url";

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

    @GetMapping("/client/connect")
    public void connect(HttpServletRequest request, HttpServletResponse response, FederatedUser federatedUser, @RequestParam("redirectUrl") String redirectUrl) throws IOException {
        LOG.debug("Starting ORCID connection linking for {} with redirect {}", federatedUser.uid, redirectUrl);
        federatedUser.setRedirectURI(redirectUrl);
        String state = URLEncoder.encode(redirectUrl, "UTF-8");
        String uri = String.format("%s?client_id=%s&response_type=code&scope=/authenticate&redirect_uri=%s&state=%s",
            orcidAuthorizationUri, orcidClientId, orcidRedirectUri, state);
        response.sendRedirect(uri);
    }

    @GetMapping("/redirect")
    public void redirect(HttpServletRequest request, HttpServletResponse response, FederatedUser federatedUser,
                         @RequestParam("code") String code,
                         @RequestParam(value = "state") String state) throws IOException {
        LOG.debug("Redirect from ORCID for {} with code {} and state {}", federatedUser.uid, code, state);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("client_id", orcidClientId);
        map.add("client_secret", orcidClientSecret);
        map.add("grant_type", "authorization_code");
        map.add("code", code);
        map.add("redirect_uri", orcidRedirectUri);

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, httpHeaders);
        ResponseEntity<Map> responseEntity = restTemplate.exchange(orcidAccessTokenUri, HttpMethod.POST, httpEntity, Map.class);
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            throw new IllegalArgumentException(String.format("Unable to exchange code for ORCID id {}", responseEntity));
        }
        String orcid = String.class.cast(responseEntity.getBody().get("orcid"));

        Optional<Account> accountOptional = accountRepository.findByUrnIgnoreCaseAndAccountType(federatedUser.uid, AccountType.ORCID);
        Account account = accountOptional.orElseGet(() -> new Account(federatedUser.uid, federatedUser.displayName, federatedUser.schacHomeOrganization, AccountType.ORCID));
        account.setLinkedId(orcid);

        accountRepository.save(account);

        LOG.debug("Saved ORCID linked account {}", account);

        response.sendRedirect(state);
    }

    @GetMapping("/internal/accounts/{urn}")
    public List<Account> accounts(@PathVariable("urn") String urn) {
        List<Account> accounts = accountRepository.findByUrnIgnoreCase(urn);
        LOG.debug("Accounts {} for {}", accounts, urn);
        return accounts;
    }

    @DeleteMapping("/internal/disconnect/{id}")
    public void disconnect(@PathVariable("id") Long id) {
        Account account = accountRepository.findOne(id);
        if (account == null) {
            throw new ResourceNotFoundException(String.format("Account %s not found", id));
        }
        LOG.debug("Deleting account {}", account);
        accountRepository.delete(account);
    }


}
