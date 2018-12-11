package aa.aggregators.sab;

import aa.aggregators.AbstractAttributeAggregator;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.UserAttribute;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class SabAttributeAggregator extends AbstractAttributeAggregator {

    private static final DateTimeFormatter dateTimeFormatter = ISODateTimeFormat.dateTimeNoMillis().withZone(DateTimeZone.UTC);
    private final String template;

    private final SabResponseParser parser = new SabResponseParser();

    public SabAttributeAggregator(AttributeAuthorityConfiguration attributeAuthorityConfiguration) {
        super(attributeAuthorityConfiguration);
        try {
            this.template = IOUtils.toString(new ClassPathResource("sab/request.xml").getInputStream(), Charset.defaultCharset());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<UserAttribute> aggregate(List<UserAttribute> input) {
        String userId = getUserAttributeSingleValue(input, NAME_ID);
        String request = request(userId);
        ResponseEntity<String> response = getRestTemplate().exchange(endpoint(), HttpMethod.POST, new HttpEntity<>(request), String.class);
        Map<SabInfoType, List<String>> result;
        try {
            String body = response.getBody();
            result = parser.parse(new StringReader(body));
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }

        LOG.debug("Retrieved SAB roles with request: {} and response: {}", request, response);

        List<String> scopedValues = result.entrySet().stream()
            .map(this::sabInfoTypeList).flatMap(Collection::stream)
            .collect(toList());

        return mapValuesToUserAttribute(EDU_PERSON_ENTITLEMENT, scopedValues);
    }

    private List<String> sabInfoTypeList(Map.Entry<SabInfoType, List<String>> entry) {
        SabInfoType sabInfoType = entry.getKey();
        List<String> values = entry.getValue();
        return values.stream().map(value ->
            value.startsWith(sabInfoType.getPrefix()) ? value : sabInfoType.getPrefix().concat(value)).collect(toList());
    }

    private String request(String userId) {
        String issueInstant = dateTimeFormatter.print(System.currentTimeMillis());
        return MessageFormat.format(template, UUID.randomUUID().toString(), issueInstant, userId);
    }

}
