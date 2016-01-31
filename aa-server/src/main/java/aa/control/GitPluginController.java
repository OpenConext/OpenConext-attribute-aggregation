package aa.control;

import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Properties;

@RestController
public class GitPluginController {

  private final Properties props;

  public GitPluginController() throws IOException {
    this.props = new Properties();
    props.load(new ClassPathResource("git.properties").getInputStream());

  }

  @RequestMapping(method = RequestMethod.GET, value = "/public/git")
  public Properties git() throws IOException {
    return props;
  }


}
