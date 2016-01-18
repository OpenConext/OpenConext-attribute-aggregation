package aa.aggregators;

import aa.model.UserAttribute;

import java.util.List;
import java.util.Optional;

public interface AttributeAggregator {

  String NAME_ID = "urn:oasis:names:tc:SAML:1.1:nameid-format:unspecified";
  String EDU_PERSON_ENTITLEMENT = "urn:mace:dir:attribute-def:eduPersonEntitlement";
  String EDU_PERSON_PRINCIPAL_NAME = "urn:mace:dir:attribute-def:eduPersonPrincipalName";
  String GROUP = "urn:collab:group";
  String ORCID = "urn:mace:dir:attribute-def:orcid";

  String getAttributeAuthorityId();

  List<UserAttribute> aggregate(List<UserAttribute> input);

  Optional<String> cacheKey(List<UserAttribute> input);

  List<String> attributeKeysRequired() ;

}
