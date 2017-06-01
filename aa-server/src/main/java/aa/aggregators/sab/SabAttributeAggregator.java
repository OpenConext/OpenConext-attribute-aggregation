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
import java.util.List;
import java.util.UUID;

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
        List<String> roles;
        try {
            roles = parser.parse(new StringReader(response.getBody()));
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        LOG.debug("Retrieved SAB roles with request: {} and response: {}", request, response);
        List<String> scopedRoles = roles.stream().map("urn:x-surfnet:surfnet.nl:sab:role:"::concat).collect(toList());
        return mapValuesToUserAttribute(EDU_PERSON_ENTITLEMENT, scopedRoles);
    }

    private String request(String userId) {
        String issueInstant = dateTimeFormatter.print(System.currentTimeMillis());
        return MessageFormat.format(template, UUID.randomUUID().toString(), issueInstant, userId);
    }

}
