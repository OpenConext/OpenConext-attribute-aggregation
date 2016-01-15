package aa.aggregators.sab;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class SabResponseParserTest {

  private SabResponseParser subject = new SabResponseParser();

  @Test
  public void testParse() throws IOException, XMLStreamException {
    String soap = IOUtils.toString(new ClassPathResource("sab/response_success.xml").getInputStream());
    List<String> roles = subject.parse(new StringReader(soap));
    assertEquals(Arrays.asList(
        "Superuser", "Instellingsbevoegde", "Infraverantwoordelijke", "OperationeelBeheerder", "Mailverantwoordelijke",
        "Domeinnamenverantwoordelijke", "DNS-Beheerder", "AAIverantwoordelijke", "Beveiligingsverantwoordelijke"),
        roles);
  }

}