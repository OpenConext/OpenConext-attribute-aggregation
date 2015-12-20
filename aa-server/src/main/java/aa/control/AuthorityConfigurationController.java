package aa.control;

import aa.config.AuthorityConfiguration;
import aa.config.AuthorityResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class AuthorityConfigurationController {

  private final AuthorityResolver authorityResolver;

  @Autowired
  public AuthorityConfigurationController(AuthorityResolver authorityResolver) {
    this.authorityResolver = authorityResolver;
  }

  @RequestMapping(method = RequestMethod.GET, value = "/internal/authorityConfiguration")
  public AuthorityConfiguration authorityConfiguration() {
    return authorityResolver.getConfiguration();
  }

}
