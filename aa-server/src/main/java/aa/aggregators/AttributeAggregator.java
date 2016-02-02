package aa.aggregators;

import aa.model.UserAttribute;

import java.util.List;
import java.util.Optional;

public interface AttributeAggregator {

  //input attribute names
  String NAME_ID = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
  String EDU_PERSON_PRINCIPAL_NAME = "urn:mace:dir:attribute-def:eduPersonPrincipalName";

  //output attribute names
  String EDU_PERSON_ENTITLEMENT = "urn:mace:dir:attribute-def:eduPersonEntitlement";
  String IS_MEMBER_OF = "urn:mace:dir:attribute-def:isMemberOf";
  String ORCID = "urn:mace:dir:attribute-def:eduPersonOrcid";

  //Schac home
  String SCHAC_HOME = "urn:mace:terena.org:attribute-def:schacHomeOrganization";

  String getAttributeAuthorityId();

  List<UserAttribute> aggregate(List<UserAttribute> input);

  Optional<String> cacheKey(List<UserAttribute> input);

  List<String> attributeKeysRequired() ;

}
