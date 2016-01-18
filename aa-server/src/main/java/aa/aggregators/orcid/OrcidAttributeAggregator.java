package aa.aggregators.orcid;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.springframework.util.CollectionUtils;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static aa.util.StreamUtils.singletonOptionalCollector;
import static java.util.stream.Collectors.toList;
import static org.springframework.web.util.UriComponentsBuilder.fromUri;

public class OrcidAttributeAggregator extends AbstractAttributeAggregator {

  private final Pattern orcidValuePattern = Pattern.compile("\\QStringAttributeValue{value=\\E(.*?)}");
  private final String requester;

  public OrcidAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration, String serverEnvironment) {
    super(attributeAuthorityConfiguration);
    this.requester = String.format("https://aa.%s.nl", serverEnvironment);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<UserAttribute> aggregate(List<UserAttribute> input) {
    String eppn = getUserAttributeSingleValue(input, EDU_PERSON_PRINCIPAL_NAME);
    URI uri = fromUri(endpoint()).queryParam("requester", requester).queryParam("principal", eppn).build().encode().toUri();
    Map<String, Object> body = getRestTemplate().getForEntity(uri, Map.class).getBody();
    List<Map<String, Object>> attributes = (List<Map<String, Object>>) body.get("attributes");
    if (!CollectionUtils.isEmpty(attributes)) {
      Optional<Map<String, Object>> optional = attributes.stream().filter(map -> "orcid".equals(map.get("name"))).collect(singletonOptionalCollector());
      if (optional.isPresent()) {
        List<String> values = (List<String>) optional.get().get("values");
        if (!CollectionUtils.isEmpty(values)) {
          List<String> orcidValues = values.stream().map(this::extractOrcidValue).filter(Optional::isPresent).map(Optional::get).collect(toList());
          LOG.debug("Retrieved ORCID with request: {} and response: {}", uri, orcidValues);
          return mapValuesToUserAttribute(ORCID, orcidValues);
        }
      }
    }
    return Collections.emptyList();
  }

  private Optional<String> extractOrcidValue(String value) {
    Matcher matcher = orcidValuePattern.matcher(value);
    return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
  }
}
