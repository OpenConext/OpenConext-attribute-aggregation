package aa.aggregators.manage;

import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SurfCrmAttributeAggregator extends ManageAttributeAggregator {

    private final ManageConfig manageConfig;

    public SurfCrmAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        super(attributeAuthorityConfiguration);
        this.manageConfig = new ManageConfig("IDPentityID", "entityid",
                "metaDataFields.coin:institution_guid", "saml20_idp");
    }

    @Override
    protected List<UserAttribute> processResult(List<Map> result) {
        String institutionGuid = getMetaDataValue(result, "coin:institution_guid");
        return StringUtils.isEmpty(institutionGuid) ? Collections.emptyList() :
                mapValuesToUserAttribute(SURF_CRM_ID, Collections.singletonList(institutionGuid));
    }

    @Override
    protected ManageConfig manageConfig() {
        return manageConfig;
    }


}
