package aa.aggregators.entitlements;

import aa.AbstractIntegrationTest;
import aa.model.ArpAggregationRequest;
import aa.model.ArpValue;
import aa.model.UserAttribute;
import org.junit.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static aa.aggregators.AttributeAggregator.EDU_PERSON_ENTITLEMENT;
import static aa.aggregators.AttributeAggregator.EDU_PERSON_PRINCIPAL_NAME;
import static aa.aggregators.AttributeAggregator.NAME_ID;
import static aa.aggregators.AttributeAggregator.SCHAC_HOME_ORGANIZATION;
import static aa.aggregators.AttributeAggregator.UID;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class EntitlementsAggregatorIntegrationTest extends AbstractIntegrationTest {

    @Override
    protected boolean isBasicAuthenticated() {
        return true;
    }

    @Test
    public void aggregateWithArpMultipleInputParameters() throws Exception {
        ResponseEntity<List<UserAttribute>> response = doAggregate(Arrays.asList(
                new UserAttribute(EDU_PERSON_PRINCIPAL_NAME, singletonList("eppn")),
                new UserAttribute(UID, singletonList("uid")),
                new UserAttribute(SCHAC_HOME_ORGANIZATION, singletonList("surfnet"))
        ));

        List<UserAttribute> userAttributes = response.getBody();
        assertEquals(1, userAttributes.size());

        UserAttribute userAttribute = userAttributes.get(0);
        assertEquals(EDU_PERSON_ENTITLEMENT, userAttribute.getName());
        assertEquals(singletonList("urn:x-surfnet:aa3:test"), userAttribute.getValues());
        assertEquals("aa3", userAttribute.getSource());
    }

    @Test
    public void aggregateWithArpMissingInputParameters() throws Exception {
        ResponseEntity<List<UserAttribute>> response = doAggregate(Arrays.asList(
                new UserAttribute(NAME_ID, singletonList("uid")),
                new UserAttribute(SCHAC_HOME_ORGANIZATION, singletonList("surfnet"))
        ));

        List<UserAttribute> userAttributes = response.getBody();
        assertEquals(0, userAttributes.size());
    }

    private ResponseEntity<List<UserAttribute>> doAggregate(List<UserAttribute> userAttributes) throws URISyntaxException {
        Map<String, List<ArpValue>> arp = new HashMap<>();
        arp.put(EDU_PERSON_ENTITLEMENT, Arrays.asList(new ArpValue("*", "aa3")));
        ArpAggregationRequest arpAggregationRequest = new ArpAggregationRequest(userAttributes, arp);

        RequestEntity<ArpAggregationRequest> requestEntity = new RequestEntity<>(arpAggregationRequest, headers, HttpMethod.POST,
                new URI("http://localhost:" + port + "/aa/api/client/attribute/aggregation"));

        ResponseEntity<String> re = restTemplate.exchange(requestEntity, String.class);

        ResponseEntity<List<UserAttribute>> responseEntity = restTemplate.exchange(requestEntity, new ParameterizedTypeReference<List<UserAttribute>>() {
        });
        assertEquals(200, responseEntity.getStatusCodeValue());

        return responseEntity;
    }


}
