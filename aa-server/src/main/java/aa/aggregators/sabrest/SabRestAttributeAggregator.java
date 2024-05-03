package aa.aggregators.sabrest;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.ArpValue;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("unchecked")
public class SabRestAttributeAggregator extends AbstractAttributeAggregator {

    public SabRestAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        super(attributeAuthorityConfiguration);
    }

    @Override

    public List<UserAttribute> aggregate(List<UserAttribute> input, Map<String, List<ArpValue>> arpAttributes) {
        String uid = getUserAttributeSingleValue(input, UID);
        String idp = getUserAttributeSingleValue(input, IDP_ENTITY_ID);
        //https://sab-ng.surfnet.nl/api/profile?uid=henny&idp=surfnet.nl
        String endpoint = getAttributeAuthorityConfiguration().getEndpoint();
        if (!endpoint.endsWith("/")) {
            endpoint += "/";
        }
        endpoint += "api/profile?uid={uid}&idp={idp}";
        Map result = getRestTemplate().getForObject(endpoint, Map.class, uid, idp);
        if (!result.containsKey("message") || !result.get("message").equals("OK")) {
            return emptyList();
        }
        List<Map<String, Object>> profiles = (List<Map<String, Object>>) result.get("profiles");
        if (CollectionUtils.isEmpty(profiles)) {
            return emptyList();
        }
        Map<String, Object> profile = profiles.get(0);
        //Must be mutable to add all
        List<String> entitlements = new ArrayList<>();
        entitlements.addAll(getOrganisationEntitlements(profile));
        entitlements.addAll(getAuthorisationEntitlements(profile));

        if (CollectionUtils.isEmpty(entitlements)) {
            return emptyList();
        }
        List<UserAttribute> userAttributes = new ArrayList<>();
        if (arpAttributes.containsKey(EDU_PERSON_ENTITLEMENT)) {
            userAttributes.add(new UserAttribute(EDU_PERSON_ENTITLEMENT,
                    entitlements.stream().sorted().collect(toList()),
                    getAttributeAuthorityId()));
        }
        if (arpAttributes.containsKey(SURF_AUTORISATIES)) {
            userAttributes.add(new UserAttribute(SURF_AUTORISATIES,
                    entitlements.stream().sorted().collect(toList()),
                    getAttributeAuthorityId()));
        }
        return userAttributes;
    }

    private List<String> getOrganisationEntitlements(Map<String, Object> profile) {
        if (profile.containsKey("organisation")) {
            Map<String, String> organisation = (Map<String, String>) profile.get("organisation");
            return Map.of("abbrev", "urn:mace:surfnet.nl:surfnet.nl:sab:organizationCode:",
                            "guid", "urn:mace:surfnet.nl:surfnet.nl:sab:organizationGUID:")
                    .entrySet()
                    .stream()
                    .filter(entry -> organisation.containsKey(entry.getKey()))
                    .map(entry -> entry.getValue() + organisation.get(entry.getKey()))
                    .collect(toList());
        }
        return new ArrayList<>();
    }

    private List<String> getAuthorisationEntitlements(Map<String, Object> profile) {
        if (profile.containsKey("authorisations")) {
            List<Map<String, String>> authorisations = (List<Map<String, String>>) profile.get("authorisations");
            return authorisations.stream()
                    .map(m -> "urn:mace:surfnet.nl:surfnet.nl:sab:role:" + m.get("role"))
                    .collect(toList());
        }
        return new ArrayList<>();
    }

}
