package aa.lifecycle;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
public class LifeCycleResult {

  private final String status = "OK";
  private final String name = "OpenConext-attribute-aggregation-server";
  @Setter
  private List<Attribute> data = new ArrayList<>();

}
