package aa.service;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ServiceProviderTranslationServiceTest {

  private ServiceProviderTranslationService subject = new ServiceProviderTranslationService();

  @Test
  public void testTranslates() throws Exception {
    String spEntityId = "https://urn:some@user:com";

    String clientId = subject.translateServiceProviderEntityId(spEntityId);
    assertEquals("https@//urn@some@@user@com", clientId);

    String toEntityIdAgain = subject.translateClientId(clientId);
    assertEquals(spEntityId, toEntityIdAgain);
  }

}