package aa.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@ToString
public class ArpAggregationRequest {

    @NotNull
    @Size(min = 1)
    private List<UserAttribute> userAttributes;

    @NotNull
    @Size
    private Map<String, List<ArpValue>> arpAttributes;

}
