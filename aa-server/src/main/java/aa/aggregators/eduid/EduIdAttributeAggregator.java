package aa.aggregators.eduid;

import aa.aggregators.AbstractAttributeAggregator;
import aa.aggregators.ala.AbstractAlaAttributeAggregator;
import aa.model.ArpValue;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EduIdAttributeAggregator extends AbstractAlaAttributeAggregator {

    public EduIdAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        super(attributeAuthorityConfiguration);
    }

    @Override
    public String arpSourceValue() {
        return "eduid";
    }

    @Override
    public boolean decodeRequestParameters() {
        return false;
    }

    @Override
    public boolean fallBackForMissingAttributesToUserAttributes() {
        return false;
    }

}
