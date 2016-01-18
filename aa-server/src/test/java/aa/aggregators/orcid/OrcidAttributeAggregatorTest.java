package aa.aggregators.orcid;

import aa.aggregators.sab.SabAttributeAggregator;
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

import static aa.aggregators.AttributeAggregator.*;
import static aa.aggregators.AttributeAggregator.NAME_ID;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static java.util.Collections.singletonList;
import static org.apache.commons.codec.binary.Base64.encodeBase64String;
import static org.junit.Assert.*;

public class OrcidAttributeAggregatorTest {

  private OrcidAttributeAggregator subject;

  private List<UserAttribute> input = singletonList(new UserAttribute(EDU_PERSON_PRINCIPAL_NAME, singletonList("urn")));

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8889);

  @Before
  public void before() {
    AttributeAuthorityConfiguration configuration = new AttributeAuthorityConfiguration("orcid");
    configuration.setEndpoint("http://localhost:8889/orcid");
    configuration.setRequiredInputAttributes(singletonList(new RequiredInputAttribute(EDU_PERSON_PRINCIPAL_NAME)));
    subject = new OrcidAttributeAggregator(configuration, "test.surfconext");
  }

  @Test
  public void testGetOrcidHappyFlow() throws Exception {
    String response = IOUtils.toString(new ClassPathResource("orcid/response_succes.json").getInputStream());
    stubForOrcid(response);
    List<UserAttribute> userAttributes = subject.aggregate(input);
    assertEquals(1, userAttributes.size());
    UserAttribute userAttribute = userAttributes.get(0);
    assertEquals(ORCID, userAttribute.getName());

    List<String> values = userAttribute.getValues();
    assertEquals(1, values.size());
    assertEquals("http://orcid.org/0000-0002-4926-2859", values.get(0));
  }

  @Test
  public void testGetOrcidEmpty() throws Exception {
    //if something goes wrong, we just don't get the orcid. We log all requests and responses
    String response = IOUtils.toString(new ClassPathResource("orcid/response_empty.json").getInputStream());
    stubForOrcid(response);
    assertTrue(subject.aggregate(input).isEmpty());
  }

  private void stubForOrcid(String response) {
    stubFor(get(urlPathEqualTo("/orcid")).willReturn(aResponse().withStatus(200).withBody(response).withHeader("Content-Type", "application/json")));
  }
}