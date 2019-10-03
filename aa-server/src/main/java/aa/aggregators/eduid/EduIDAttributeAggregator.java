package aa.aggregators.eduid;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public class EduIDAttributeAggregator extends AbstractAttributeAggregator {

    public EduIDAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        super(attributeAuthorityConfiguration);
    }

    @Override
    public List<UserAttribute> aggregate(List<UserAttribute> input) {
        String eduPersonPrincipalName = getUserAttributeSingleValue(input, EDU_PERSON_PRINCIPAL_NAME);

        String endPoint = UriComponentsBuilder.fromHttpUrl(getAttributeAuthorityConfiguration().getEndpoint())
                .queryParam("edu_person_principal_name", eduPersonPrincipalName).toUriString();

        return (List<UserAttribute>) this.getRestTemplate().getForEntity(endPoint, List.class).getBody();
    }

    @Override
    public List<UserAttribute> filterInvalidResponses(List<UserAttribute> input) {
        return input;
    }
}
