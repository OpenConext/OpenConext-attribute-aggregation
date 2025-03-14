package aa.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ArpAggregationRequest {

    @NotNull
    @Size(min = 1)
    private List<UserAttribute> userAttributes;

    @NotNull
    @Size
    private Map<String, List<ArpValue>> arpAttributes;

}
