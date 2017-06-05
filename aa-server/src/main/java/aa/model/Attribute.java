package aa.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@ToString
public class Attribute implements Cloneable {

    private Long id;

    private String attributeAuthorityId;

    private String name;

    private boolean skipConsent;

    private boolean caseExact;

    private String description;

    private boolean multiValued;

    private String mutability;

    private boolean required;

    private String returned;

    private String type;

    private String uniqueness;

    @Override
    public Attribute clone() {
        try {
            return (Attribute) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
