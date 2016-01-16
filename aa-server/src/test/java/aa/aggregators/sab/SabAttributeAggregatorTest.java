package aa.aggregators.sab;

import aa.aggregators.AttributeAggregator;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.RequiredInputAttribute;
import aa.model.UserAttribute;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static aa.aggregators.AttributeAggregator.EDU_PERSON_ENTITLEMENT;
import static aa.aggregators.AttributeAggregator.NAME_ID;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Collections.singletonList;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SabAttributeAggregatorTest {

  private SabAttributeAggregator subject;

  private List<UserAttribute> input = singletonList(new UserAttribute(NAME_ID, singletonList("urn")));

  @Before
  public void before() {
    AttributeAuthorityConfiguration configuration = new AttributeAuthorityConfiguration("sab");
    configuration.setUser("user");
    configuration.setPassword("password");
    configuration.setEndpoint("http://localhost:8889/sab");
    configuration.setRequiredInputAttributes(singletonList(new RequiredInputAttribute(NAME_ID)));
    subject = new SabAttributeAggregator(configuration);
  }

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8889);

  @Test
  public void testGetRolesHappyFlow() throws Exception {
    String response = IOUtils.toString(new ClassPathResource("sab/response_success.xml").getInputStream());
    stubFor(post(urlEqualTo("/sab")).withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
        .willReturn(aResponse().withStatus(200).withBody(response)));
    List<UserAttribute> userAttributes = subject.aggregate(input);
    assertEquals(1, userAttributes.size());
    UserAttribute userAttribute = userAttributes.get(0);
    assertEquals(EDU_PERSON_ENTITLEMENT, userAttribute.getName());

    List<String> expected = Arrays.asList("urn:x-surfnet:sab:Superuser", "urn:x-surfnet:sab:Instellingsbevoegde", "urn:x-surfnet:sab:Infraverantwoordelijke",
        "urn:x-surfnet:sab:OperationeelBeheerder", "urn:x-surfnet:sab:Mailverantwoordelijke", "urn:x-surfnet:sab:Domeinnamenverantwoordelijke",
        "urn:x-surfnet:sab:DNS-Beheerder", "urn:x-surfnet:sab:AAIverantwoordelijke", "urn:x-surfnet:sab:Beveiligingsverantwoordelijke");
    assertEquals(expected, userAttribute.getValues());
  }

  @Test
  public void testGetRolesFailures() throws Exception {
    //if something goes wrong, we just don't get roles. We log all requests and responses
    for (String fileName : Arrays.asList("response_acl_blocked.xml", "response_invalid_user.xml", "response_unknown_user.xml")) {
      //lambda requires error handling
      assertEmptyRoles(fileName);
    }
  }

  private void assertEmptyRoles(String fileName) throws IOException {
    String response = IOUtils.toString(new ClassPathResource("sab/" + fileName).getInputStream());
    stubFor(post(urlEqualTo("/sab")).withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
        .willReturn(aResponse().withStatus(200).withBody(response)));
    assertTrue(subject.aggregate(input).isEmpty());
  }
}