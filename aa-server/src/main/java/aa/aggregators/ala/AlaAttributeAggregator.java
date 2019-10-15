package aa.aggregators.ala;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.ArpValue;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AlaAttributeAggregator extends AbstractAttributeAggregator {

    private HttpHeaders httpHeaders = new HttpHeaders();

    public AlaAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        super(attributeAuthorityConfiguration);
        this.httpHeaders.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        this.httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    }

    @Override
    public List<UserAttribute> aggregate(List<UserAttribute> input, Map<String, List<ArpValue>> arpAttributes) {
        String eduPersonPrincipalName = getUserAttributeSingleValue(input, EDU_PERSON_PRINCIPAL_NAME);
        String spEntityId = getUserAttributeSingleValue(input, SP_ENTITY_ID);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("edu_person_principal_name", eduPersonPrincipalName);
        form.add("sp_entity_id", spEntityId);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, httpHeaders);

        String endPoint = getAttributeAuthorityConfiguration().getEndpoint();
        List<UserAttribute> userAttributes = this.getRestTemplate().exchange(endPoint, HttpMethod.POST,
                request, new ParameterizedTypeReference<List<UserAttribute>>() {
                }).getBody();
        List<String> userAttributesNames = userAttributes.stream().map(UserAttribute::getName).collect(Collectors.toList());

        //For all non-present values in the userAttributes we fall back to the values provided - if present and marked as 'ala' source
        List<String> arpKeys = arpAttributes.keySet().stream()
                .filter(samlAttributeName -> !userAttributesNames.contains(samlAttributeName) &&
                        arpAttributes.get(samlAttributeName).stream().anyMatch(arpValue -> "ala".equals(arpValue.getSource())))
                .collect(Collectors.toList());
        List<UserAttribute> preserve = input.stream().filter(userAttribute -> arpKeys.contains(userAttribute.getName())).collect(Collectors.toList());
        userAttributes.addAll(preserve);
        userAttributes.forEach(userAttribute -> userAttribute.setSource(getAttributeAuthorityId()));
        return userAttributes;
    }

    @Override
    public List<UserAttribute> filterInvalidResponses(List<UserAttribute> input) {
        return input;
    }
}
