package aa.serviceregistry;

import aa.model.ServiceProvider;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static junit.framework.TestCase.assertEquals;
import static org.springframework.http.HttpHeaders.IF_MODIFIED_SINCE;

public class UrlResourceServiceRegistryTest {

  private UrlResourceServiceRegistry subject;

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8889);

  @Before
  public void before() throws IOException {
    doBefore("service-registry/service-providers.json");
  }

  @Test
  public void testMetaData() throws Exception {
    assertEquals(4, subject.serviceProviders().size());
  }

  @Test
  public void testInitializeMetaDataNotModifed() throws IOException {
    stubFor(get(urlEqualTo("/sp")).willReturn(aResponse().withStatus(500)));
    stubFor(head(urlEqualTo("/sp")).withHeader(IF_MODIFIED_SINCE, notMatching("X")).willReturn(aResponse().withStatus(304)));
    subject.initializeMetadata();
  }

  @Test
  public void testSorting() throws IOException {
    doBefore("service-registry-test/service-providers.json");
    List<ServiceProvider> serviceProviders = new ArrayList<>(subject.serviceProviders());

    assertEquals("Bas Test-SP", serviceProviders.get(0).getName());
    assertEquals("OpenConext PDP", serviceProviders.get(serviceProviders.size() - 1).getName());

  }

  private void doBefore(String path) throws IOException {
    String spResponse = IOUtils.toString(new ClassPathResource(path).getInputStream());
    stubFor(get(urlEqualTo("/sp")).willReturn(aResponse().withStatus(200).withHeader("Content-Type", "application/json").withBody(spResponse)));

    stubFor(head(urlEqualTo("/sp")).withHeader(IF_MODIFIED_SINCE, notMatching("X")).willReturn(aResponse().withStatus(200)));

    this.subject = (UrlResourceServiceRegistry) new ServiceRegistryConfiguration().urlResourceServiceRegistry("user","password","http://localhost:8889/sp", 10);
  }

}