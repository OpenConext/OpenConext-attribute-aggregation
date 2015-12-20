package aa.control;

import aa.shibboleth.FederatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class UserController {

  private final static Logger LOG = LoggerFactory.getLogger(UserController.class);

  @RequestMapping(method = GET, value = "internal/users/me")
  public FederatedUser user() {
    FederatedUser federatedUser = (FederatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    LOG.debug("Returning user {}", federatedUser);
    return federatedUser;
  }

}
