package aa.aggregators;

import aa.model.ArpValue;
import aa.model.UserAttribute;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AttributeAggregator {

    //input attribute names
    String NAME_ID = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
    String UID = "urn:mace:dir:attribute-def:uid";
    String EDU_PERSON_PRINCIPAL_NAME = "urn:mace:dir:attribute-def:eduPersonPrincipalName";
    String SCHAC_HOME_ORGANIZATION = "urn:mace:terena.org:attribute-def:schacHomeOrganization";
    String EMAIL = "urn:mace:dir:attribute-def:mail";
    String SP_ENTITY_ID = "SPentityID";
    String IDP_ENTITY_ID = "IDPentityID";

    //output attribute names
    String EDU_PERSON_ENTITLEMENT = "urn:mace:dir:attribute-def:eduPersonEntitlement";
    String EDU_PERSON_AFFILIATION = "urn:mace:dir:attribute-def:eduPersonAffiliation";
    String IS_MEMBER_OF = "urn:mace:dir:attribute-def:isMemberOf";
    String ORCID = "urn:mace:dir:attribute-def:eduPersonOrcid";
    String SURF_CRM_ID = "urn:mace:surf.nl:attribute-def:surf-crm-id";
    String SURF_AUTORISATIES = "urn:mace:surf.nl:attribute-def:surf-autorisaties";

    String getAttributeAuthorityId();

    List<UserAttribute> aggregate(List<UserAttribute> input, Map<String, List<ArpValue>> arpAttributes);

    Optional<String> cacheKey(List<UserAttribute> input);

    List<String> attributeKeysRequired();

    List<UserAttribute> filterInvalidResponses(List<UserAttribute> input);

    boolean cachingEnabled();

}
