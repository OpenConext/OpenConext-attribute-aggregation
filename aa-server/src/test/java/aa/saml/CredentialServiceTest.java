package aa.saml;

import org.junit.Test;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;

import static org.junit.Assert.*;

public class CredentialServiceTest {

  @Test
  public void testGetCredential() throws Exception {
    Properties properties = new Properties();
    properties.load(new ClassPathResource("application.properties").getInputStream());
    CredentialService subject = new CredentialService(
        properties.getProperty("aa.public.certificate"),
        properties.getProperty("aa.private.key"),
        properties.getProperty("aa.entityId"));

    BasicX509Credential credential = subject.getCredential();
    PrivateKey privateKey = credential.getPrivateKey();
    PublicKey publicKey = credential.getPublicKey();

    assertNotNull(privateKey);
    assertNotNull(publicKey);
  }
}