package aa.control;

import aa.shibboleth.FederatedUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@RestController
@RequestMapping(produces = {"application/json"})
public class UserController {

    private final static Logger LOG = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private ObjectMapper objectMapper;

    @GetMapping("/client/users/me")
    public FederatedUser user() {
        FederatedUser federatedUser = (FederatedUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        LOG.debug("Returning user {}", federatedUser);
        return federatedUser;
    }

    @DeleteMapping("/client/users/logout")
    public void logout(HttpServletRequest request) {
        request.getSession().invalidate();
        SecurityContextHolder.clearContext();
    }

    @PostMapping("/client/error")
    public void error(@RequestBody Map<String, Object> payload, FederatedUser federatedUser) throws JsonProcessingException, UnknownHostException {
        payload.put("dateTime", new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss").format(new Date()));
        payload.put("machine", InetAddress.getLocalHost().getHostName());
        payload.put("user", federatedUser);
        String msg = objectMapper.writeValueAsString(payload);
        LOG.error(msg, new IllegalArgumentException(msg));
    }


}
