package aa.control;


import aa.model.Account;
import aa.model.AccountType;
import aa.model.ResourceNotFoundException;
import aa.model.UnauthorizedException;
import aa.repository.AccountRepository;
import aa.shibboleth.FederatedUser;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class AccountController {

    private final static Logger LOG = LoggerFactory.getLogger(AccountController.class);

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

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final RestTemplate restTemplate = new RestTemplate();

    private final HttpHeaders httpHeaders = new HttpHeaders();

    public AccountController() {
        this.httpHeaders.add(HttpHeaders.ACCEPT, "application/json");
    }

    @GetMapping("/client/connect")
    public void connect(HttpServletResponse response, FederatedUser federatedUser,
                        @RequestParam(value = "redirectUrl", required = false) String redirectUrl) throws IOException {
        LOG.debug("Starting ORCID connection linking for {} with redirect {}", federatedUser.uid, redirectUrl);
        String state = String.format("redirect_url=%s&user_uid=%s",
                StringUtils.hasText(redirectUrl) ? redirectUrl : "",
                passwordEncoder.encode(federatedUser.uid));
        String stateEncoded = URLEncoder.encode(state, "UTF-8");
        String uri = String.format("%s?client_id=%s&response_type=code&scope=/authenticate&redirect_uri=%s&state=%s",
                orcidAuthorizationUri, orcidClientId, orcidRedirectUri, stateEncoded);
        response.sendRedirect(uri);
    }

    @GetMapping("/redirect")
    public void redirect(HttpServletResponse response,
                         FederatedUser federatedUser,
                         @RequestParam("code") String code,
                         @RequestParam("state") String state) throws IOException {
        LOG.debug("Redirect from ORCID for {} with code {} and state {}", federatedUser.uid, code, state);

        MultiValueMap<String, String> params = UriComponentsBuilder.fromHttpUrl("http://localhost?" + state).build().getQueryParams();
        String redirectUrl = params.getFirst("redirect_url");
        String encodedUserUid = params.getFirst("user_uid");

        if (!passwordEncoder.matches(federatedUser.uid, encodedUserUid)) {
            throw new UnauthorizedException("Non matching user");
        }


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
        Account account = accountOptional.orElseGet(() -> new Account(federatedUser.uid, federatedUser.schacHomeOrganization, AccountType.ORCID));
        account.setLinkedId(orcid.startsWith("http") ? orcid : "http://orcid.org/".concat(orcid));

        accountRepository.save(account);

        LOG.debug("Saved ORCID linked account {}", account);

        if (StringUtils.hasText(redirectUrl)) {
            response.sendRedirect(redirectUrl);
        } else {
            response.sendRedirect("/aa/api/client/connected.html");
        }

    }

    @GetMapping("/internal/accounts/{urn:.+}")
    public List<Account> accounts(@PathVariable("urn") String urn) {
        List<Account> accounts = accountRepository.findByUrnIgnoreCase(urn);
        LOG.debug("Accounts {} for {}", accounts, urn);
        return accounts;
    }

    @DeleteMapping("/internal/disconnect/{id}")
    public ResponseEntity<Map<String, String>> disconnect(@PathVariable("id") Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Account %s not found", id)));
        LOG.debug("Deleting account {}", account);
        accountRepository.delete(account);
        return ResponseEntity.ok(Collections.singletonMap("status", "OK"));
    }


}
