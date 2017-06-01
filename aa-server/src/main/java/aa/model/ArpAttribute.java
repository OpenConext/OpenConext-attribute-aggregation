package aa.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@AllArgsConstructor
@ToString
public class ArpAttribute {

    private String name;
    private List<ArpValue> values;
}
