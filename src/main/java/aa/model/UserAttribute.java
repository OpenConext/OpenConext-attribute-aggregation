package aa.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public class UserAttribute {

    private String name;
    private List<String> values;
    private String source;

    public UserAttribute(String name, List<String> values) {
        this.name = name;
        this.values = values;
    }

}
