package aa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
public class AttributeAuthorityConfiguration {

    private String id;
    private String description;
    private String endpoint;
    private String user;
    private List<Attribute> attributes;
    private List<RequiredInputAttribute> requiredInputAttributes = new ArrayList<>();
    private int timeOut;
    private String validationRegExp;
    @JsonIgnore
    private String password;

    public AttributeAuthorityConfiguration(String id) {
        this.id = id;
    }

    public AttributeAuthorityConfiguration(String id, List<Attribute> attributes, String validationRegExp) {
        this.id = id;
        this.attributes = attributes;
        this.validationRegExp = validationRegExp;
    }


}
