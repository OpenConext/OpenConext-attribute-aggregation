package aa.aggregators.sbs;

import aa.aggregators.AbstractAttributeAggregator;
import aa.aggregators.NotFoundResponseErrorHandler;
import aa.model.ArpValue;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SBSAttributeAggregator extends AbstractAttributeAggregator {

    public SBSAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        super(attributeAuthorityConfiguration);
        getRestTemplate().setErrorHandler(new NotFoundResponseErrorHandler());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<UserAttribute> aggregate(List<UserAttribute> input, Map<String, List<ArpValue>> arpAttributes) {
        String eduPersonPrincipalName = getUserAttributeSingleValue(input, EDU_PERSON_PRINCIPAL_NAME);
        Optional<UserAttribute> emailUserAttribute = input.stream().filter(attr -> attr.getName().equals(EMAIL))
                .findFirst();

        StringBuilder endpoint = new StringBuilder(getAttributeAuthorityConfiguration().getEndpoint().concat("?edu_person_principal_name=").concat(eduPersonPrincipalName));
        emailUserAttribute.ifPresent(userAttribute -> {
            List<String> values = userAttribute.getValues();
            if (!CollectionUtils.isEmpty(values)) {
                String s = values.get(0);
                if (StringUtils.hasText(s)) {
                    endpoint.append("&email=").append(encode(s));
                }
            }
        });
        List<String> memberShips = this.getRestTemplate().getForEntity(endpoint.toString(), List.class).getBody();

        LOG.debug("Retrieved SBS groups with request: {} and response: {}", endpoint, memberShips);

        return mapValuesToUserAttribute(IS_MEMBER_OF, memberShips);
    }
}
