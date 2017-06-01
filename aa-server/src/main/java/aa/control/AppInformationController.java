package aa.control;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(headers = {"Content-Type=application/json"}, produces = {"application/json"})
public class AppInformationController {

    private final String version;

    @Autowired
    public AppInformationController(@Value("${app.version}") String version) {
        this.version = version;
    }

    @RequestMapping(method = GET, value = "internal/appInformation")
    public Map<String, String> info() {
        return singletonMap("version", version);
    }

}
