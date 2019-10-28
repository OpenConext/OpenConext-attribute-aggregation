package aa.aggregators.manage;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.ArpValue;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class ManageAttributeAggregator extends AbstractAttributeAggregator {

    private HttpHeaders httpHeaders = new HttpHeaders();

    public ManageAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        super(attributeAuthorityConfiguration);
        this.httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        this.httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    }

    protected abstract List<UserAttribute> processResult(List<Map> result);

    protected abstract ManageConfig manageConfig();

    @Override
    public List<UserAttribute> aggregate(List<UserAttribute> input, Map<String, List<ArpValue>> arpAttributes) {
        ManageConfig manageConfig = manageConfig();

        String searchValue = getUserAttributeSingleValue(input, manageConfig.getSamlQueryParameter());
        URI endpoint = endpoint("/manage/api/internal/search/" + manageConfig.getMetaDataType());

        Map<String, Object> body = new HashMap<>();
        body.put(manageConfig.getManageQueryParameter(), searchValue);
        body.put("REQUESTED_ATTRIBUTES", Collections.singletonList(manageConfig.getRequestAttribute()));


        List<Map> result = getRestTemplate().exchange(endpoint, HttpMethod.POST, new HttpEntity<Object>(body, httpHeaders), new ParameterizedTypeReference<List<Map>>() {
        }).getBody();

        LOG.debug("Retrieved Manage information with request: {} and response: {}", body, result);
        if (CollectionUtils.isEmpty(result)) {
            return Collections.emptyList();
        }
        return processResult(result);
    }

}
