package aa.aggregators.institution;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SAMLMapping {

    private final static Map<String, String> mapping = new HashMap<>();

    static {
        mapping.put("urn:mace:dir:attribute-def:cn", "name");
        mapping.put("urn:mace:dir:attribute-def:displayName", "preferred_username");
        mapping.put("urn:mace:dir:attribute-def:givenName", "given_name");
        mapping.put("urn:mace:dir:attribute-def:sn", "family_name");
        mapping.put("urn:mace:dir:attribute-def:mail", "email");
        mapping.put("urn:mace:terena.org:attribute-def:schacHomeOrganization", "schac_home_organization");
        mapping.put("urn:mace:terena.org:attribute-def:schacHomeOrganizationType", "schac_home_organization_type");
        mapping.put("urn:mace:dir:attribute-def:eduPersonAffiliation", "eduperson_affiliation");
        mapping.put("urn:mace:dir:attribute-def:eduPersonScopedAffiliation", "eduperson_scoped_affiliation");
        mapping.put("urn:mace:dir:attribute-def:isMemberOf", "edumember_is_member_of");
        mapping.put("urn:mace:dir:attribute-def:eduPersonEntitlement", "eduperson_entitlement");
        mapping.put("urn:mace:dir:attribute-def:eduPersonPrincipalName", "eduperson_principal_name");
        mapping.put("urn:mace:surf.nl:attribute-def:eckid", "eckid");
        mapping.put("urn:schac:attribute-def:schacDateOfBirth", "date_of_birth");
        mapping.put("urn:mace:dir:attribute-def:preferredLanguage", "preferred_language");
        mapping.put("urn:mace:dir:attribute-def:uid", "uids");
    }

    public Optional<String> convertSAMLAttributeName(String samlAttributeName) {
        return Optional.ofNullable(mapping.get(samlAttributeName));
    }
}
