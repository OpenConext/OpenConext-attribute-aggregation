package aa.aggregators.sab;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class SabResponseParserTest {

    private SabResponseParser subject = new SabResponseParser();

    @Test
    public void testParse() throws IOException, XMLStreamException {
        doParse("sab/response_success.xml");
    }

    @Test
    public void testParseExtraAttribute() throws IOException, XMLStreamException {
        doParse("sab/response_success_extra_attribute.xml");
    }

    @Test
    public void testParseFull() throws IOException, XMLStreamException {
        Map<SabInfoType, List<String>> result = doParse("sab/response_success_full.xml");
        assertEquals(Arrays.asList("SURFNET"), result.get(SabInfoType.ORGANIZATION));
        assertEquals(Arrays.asList("ad93daef-0911-e511-80d0-005056956c1a"), result.get(SabInfoType.GUID));
        assertEquals(Arrays.asList("+31887873000"), result.get(SabInfoType.MOBILE));
    }

    @Test(expected = XMLStreamException.class)
    public void testParseProcessingInstruction() throws IOException, XMLStreamException {
        doParseAndOptionalAssert("sab/response_XML_processing_instruction.xml", false);
    }


    private Map<SabInfoType, List<String>> doParse(String jsonResponse) throws IOException, XMLStreamException {
        return doParseAndOptionalAssert(jsonResponse, true);
    }

    private Map<SabInfoType, List<String>> doParseAndOptionalAssert(String jsonResponse, boolean assertRoles) throws IOException, XMLStreamException {
        String soap = IOUtils.toString(new ClassPathResource(jsonResponse).getInputStream(), Charset.defaultCharset());
        Map<SabInfoType, List<String>> result = subject.parse(new StringReader(soap));
        if (assertRoles) {
            assertEquals(Arrays.asList(
                    "Superuser", "Instellingsbevoegde", "Infraverantwoordelijke", "OperationeelBeheerder", "Mailverantwoordelijke",
                    "Domeinnamenverantwoordelijke", "DNS-Beheerder", "AAIverantwoordelijke", "Beveiligingsverantwoordelijke"),
                    result.get(SabInfoType.ROLE));
        }
        return result;
    }

}
