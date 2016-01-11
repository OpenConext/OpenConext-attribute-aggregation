package aa.saml;

import org.apache.commons.io.IOUtils;
import org.apache.commons.ssl.Base64;
import org.opensaml.xml.security.x509.BasicX509Credential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;

@Service
public class CredentialService {

  private BasicX509Credential credential;

  @Autowired
  public CredentialService(@Value("${aa.public.certificate}") String certificate,
                           //This must be in the DER unencrypted PKCS#8 format. See README.md
                           @Value("${aa.private.key}") String privateKey,
                           @Value("${aa.entityId}") String entityId) {
    try {
      initialize(certificate, privateKey, entityId);
    } catch (Exception e) {
      //too many exceptions we can't handle anyway, so brute force catch
      throw new RuntimeException(e);
    }
  }

  public BasicX509Credential getCredential() {
    return credential;
  }

  private void initialize(String certificate,
                          String privateKey,
                          String entityId) throws CertificateException, IOException, NoSuchAlgorithmException, InvalidKeySpecException {
    String wrappedCert = wrapCert(certificate);
    byte[] decodedKey = Base64.decodeBase64(privateKey.getBytes());

    CertificateFactory certFact = CertificateFactory.getInstance("X.509");
    X509Certificate cert = (X509Certificate) certFact.generateCertificate(new ByteArrayInputStream(wrappedCert.getBytes()));

    byte[] privateKeyBytes = IOUtils.toByteArray(new ByteArrayInputStream(decodedKey));

    KeySpec ks = new PKCS8EncodedKeySpec(privateKeyBytes);
    RSAPrivateKey privKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(ks);

    BasicX509Credential credential = new BasicX509Credential();
    credential.setEntityCertificate(cert);
    credential.setPrivateKey(privKey);
    credential.setEntityId(entityId);

    this.credential = credential;
  }

  private String wrapCert(String certificate) {
    return "-----BEGIN CERTIFICATE-----\n" + certificate + "\n-----END CERTIFICATE-----";
  }

}
