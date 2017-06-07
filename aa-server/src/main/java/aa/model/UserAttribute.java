package aa.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class UserAttribute {

    private String name;
    private List<String> values;
    private String source;

    public UserAttribute() {
    }

    public UserAttribute(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }

    public UserAttribute(String name, List<String> values, String source) {
        this.name = name;
        this.values = values;
        this.source = source;
    }

}
