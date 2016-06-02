package aa.aggregators.sab;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SabResponseParserTest {

  private SabResponseParser subject = new SabResponseParser();

  @Test
  public void testParse() throws IOException, XMLStreamException {
    dpParse("sab/response_success.xml");
  }

  @Test
  public void testParseExtraAttribute() throws IOException, XMLStreamException {
    dpParse("sab/response_success_extra_attribute.xml");
  }

  private void dpParse(String jsonResponse) throws IOException, XMLStreamException {
    String soap = IOUtils.toString(new ClassPathResource(jsonResponse).getInputStream());
    List<String> roles = subject.parse(new StringReader(soap));
    assertEquals(Arrays.asList(
        "Superuser", "Instellingsbevoegde", "Infraverantwoordelijke", "OperationeelBeheerder", "Mailverantwoordelijke",
        "Domeinnamenverantwoordelijke", "DNS-Beheerder", "AAIverantwoordelijke", "Beveiligingsverantwoordelijke"),
        roles);
  }

}