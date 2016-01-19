package aa.control;

import aa.authz.AbstractAuthzIntegrationTest;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.opensaml.Configuration;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Attribute;
import org.opensaml.saml2.core.AttributeStatement;
import org.opensaml.saml2.core.Response;
import org.opensaml.ws.soap.common.AbstractExtensibleSOAPObject;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.xml.io.Unmarshaller;
import org.opensaml.xml.io.UnmarshallerFactory;
import org.opensaml.xml.io.UnmarshallingException;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.schema.XSString;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

public class SAMLAttributeQueryControllerTest extends AbstractAuthzIntegrationTest {

  @Test
  public void testAttributeQuery() throws Exception {
    ResponseEntity<String> result = attributeQuery("saml/attribute_query.xml");
    String soap = result.getBody();

    Response response = getResponse(soap);

    assertEquals("https://engine.surfconext.nl/authentication/idp/metadata", response.getIssuer().getValue());
    assertNotNull(response.getSignature());

    Assertion assertion = response.getAssertions().get(0);
    List<AttributeStatement> statements = assertion.getAttributeStatements();
    assertEquals(1, statements.size());

    AttributeStatement attributeStatement = statements.get(0);
    assertEquals(1, attributeStatement.getAttributes().size());

    Attribute attribute = attributeStatement.getAttributes().get(0);
    assertEquals("urn:mace:dir:attribute-def:eduPersonOrcid", attribute.getName());

    List<String> values = attribute.getAttributeValues().stream().map(xmlObject -> ((XSString) xmlObject).getValue()).collect(toList());
    assertEquals(Collections.singletonList("urn:x-surfnet:aa1:test"), values);
  }

  private Response getResponse(String soap) throws XMLParserException, UnmarshallingException {
    StaticBasicParserPool parserPool = new StaticBasicParserPool();
    parserPool.setNamespaceAware(true);
    parserPool.initialize();

    Document document = parserPool.parse(new StringReader(soap));
    Element element = document.getDocumentElement();

    UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
    Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);

    AbstractExtensibleSOAPObject body = (AbstractExtensibleSOAPObject) ((Envelope) unmarshaller.unmarshall(element)).getBody();
    Response response = (Response) body.getOrderedChildren().get(0);
    return response;
  }

  @Test
  public void testAttributeQueryUnknownServiceProvider() throws Exception {
    ResponseEntity<String> result = attributeQuery("saml/attribute_query.unknown_sp.xml");

    assertEquals(NOT_FOUND, result.getStatusCode());
  }

  private ResponseEntity<String> attributeQuery(String path) throws IOException, URISyntaxException {
    String soap = IOUtils.toString(new ClassPathResource(path).getInputStream());

    oauthHeaders.remove(CONTENT_TYPE);
    oauthHeaders.add(CONTENT_TYPE, "application/soap+xml");

    RequestEntity requestEntity = new RequestEntity(soap, oauthHeaders, HttpMethod.POST, new URI("http://localhost:" + port + "/aa/api/v2/query"));
    return restTemplate.exchange(requestEntity, String.class);
  }

}