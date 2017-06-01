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
    private final Pattern orcidPattern = Pattern.compile("\\Qhttp://orcid.org/\\E[0-9]{4}-[0-9]{4}-[0-9]{4}-[0-9]{3}[0-9|X]{1}");

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

        LOG.debug("Retrieved ORCID with request: {} and response: {}", uri, body);

        List<String> orcidValues = getOrcid(body);
        return mapValuesToUserAttribute(ORCID, orcidValues.stream().filter(this::isValidOrcidId).collect(toList()));
    }

    @SuppressWarnings("unchecked")
    private List<String> getOrcid(Map<String, Object> body) {
        List<Map<String, Object>> attributes = (List<Map<String, Object>>) body.get("attributes");
        if (!CollectionUtils.isEmpty(attributes)) {
            Optional<Map<String, Object>> optional = attributes.stream().filter(map -> "orcid".equals(map.get("name"))).collect(singletonOptionalCollector());
            if (optional.isPresent()) {
                List<String> values = (List<String>) optional.get().get("values");
                if (!CollectionUtils.isEmpty(values)) {
                    return values.stream().map(this::extractOrcidValue).filter(Optional::isPresent).map(Optional::get).collect(toList());
                }
            }
        }
        return Collections.emptyList();
    }

    private Optional<String> extractOrcidValue(String value) {
        Matcher matcher = orcidValuePattern.matcher(value);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }

    private boolean isValidOrcidId(String value) {
        boolean matches = orcidPattern.matcher(value).matches();
        if (!matches) {
            LOG.warn("Received invalid ORCID {}", value);
        }
        return matches;
    }
}
