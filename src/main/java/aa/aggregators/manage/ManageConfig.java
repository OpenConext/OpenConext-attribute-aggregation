package aa.aggregators.manage;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ManageConfig {

    private String samlQueryParameter;
    private String manageQueryParameter;
    private String requestAttribute;
    private String metaDataType;

}
