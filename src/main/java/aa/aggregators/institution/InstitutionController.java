package aa.aggregators.institution;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class InstitutionController {

    private final Map<String, List<String>> userInfo = new HashMap<>();

    public InstitutionController() {
        userInfo.put("given_name", List.of("John"));
        userInfo.put("family_name", List.of("Doe"));
        userInfo.put("name", List.of("Prof.dr. John Doe"));
        userInfo.put("email", List.of("john.doe@example.com"));
        userInfo.put("ou", List.of("Faculty of Science"));
        userInfo.put("schac_home_organization", List.of("university.nl"));
        userInfo.put("eduperson_scoped_affiliation", List.of("student@uniharderwijk.nl"));
        userInfo.put("schac_personal_unique_code", List.of("S12345678"));
        userInfo.put("uids", List.of("jdoe123"));
        userInfo.put("eduperson_principal_name", List.of("johndoe@studenthartingcollege.nl"));
        userInfo.put("eduperson_entitlement", List.of("urn:x-surfnet:surf.nl:surfdrive:quota:100"));
        userInfo.put("edumember_is_member_of", List.of("research-group"));
        userInfo.put("eckid", List.of("eck123456"));
    }

    @GetMapping("/institution/mock")
    public Map<String, List<String>> mock() {
        return userInfo;
    }

}
