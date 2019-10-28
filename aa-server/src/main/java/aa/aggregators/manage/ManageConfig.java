package aa.aggregators.manage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Getter
public class ManageConfig {

    private String samlQueryParameter;
    private String manageQueryParameter;
    private String requestAttribute;
    private String metaDataType;
    private String attributeName;

}
