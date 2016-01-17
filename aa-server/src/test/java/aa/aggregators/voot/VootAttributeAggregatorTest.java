package aa.aggregators.voot;

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
import java.util.List;

import static aa.aggregators.AttributeAggregator.GROUP;
import static aa.aggregators.AttributeAggregator.NAME_ID;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Collections.singletonList;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VootAttributeAggregatorTest {

  private VootAttributeAggregator subject;

  private List<UserAttribute> input = singletonList(new UserAttribute(NAME_ID, singletonList("urn")));

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8889);

  @Before
  public void before() {
    AttributeAuthorityConfiguration configuration = new AttributeAuthorityConfiguration("voot");
    configuration.setUser("user");
    configuration.setPassword("password");
    configuration.setEndpoint("http://localhost:8889/voot");
    configuration.setRequiredInputAttributes(singletonList(new RequiredInputAttribute(NAME_ID)));
    subject = new VootAttributeAggregator(configuration, "http://localhost:8889/authorize");
    String accessTokenResponse = "{ \"access_token\"  : \"token\"}";
    stubFor(post(urlEqualTo("/authorize")).withHeader("Authorization", equalTo("Basic " + encodeBase64String("user:password".getBytes())))
        .willReturn(aResponse().withStatus(200).withBody(accessTokenResponse).withHeader("Content-Type", "application/json")));
  }

  @Test
  public void testGetRolesHappyFlow() throws Exception {
    String response = IOUtils.toString(new ClassPathResource("voot/groups.json").getInputStream());
    stubForVoot(response);
    List<UserAttribute> userAttributes = subject.aggregate(input);
    assertEquals(1, userAttributes.size());
    UserAttribute userAttribute = userAttributes.get(0);
    assertEquals(GROUP, userAttribute.getName());

    List<String> values = userAttribute.getValues();
    assertEquals(14, values.size());

    values.forEach(value -> assertTrue(value.startsWith("urn:x-surfnet:voot:urn:collab:group:test.surfteams.nl")));
  }

  @Test
  public void testGetRolesFailures() throws Exception {
    //if something goes wrong, we just don't get groups. We log all requests and responses
    for (String fileName : Arrays.asList("empty_groups.json")) {
      assertEmptyRoles(fileName);
    }
  }

  private void assertEmptyRoles(String fileName) throws IOException {
    String response = IOUtils.toString(new ClassPathResource("voot/" + fileName).getInputStream());
    stubForVoot(response);
    assertTrue(subject.aggregate(input).isEmpty());
  }

  private void stubForVoot(String response) {
    stubFor(get(urlEqualTo("/voot/internal/groups/urn")).willReturn(aResponse().withStatus(200).withBody(response).withHeader("Content-Type", "application/json")));
  }
}