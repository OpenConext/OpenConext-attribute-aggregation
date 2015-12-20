package aa.control;

import aa.model.SchemaNotFoundException;
import aa.model.ServiceProvider;
import aa.model.UserAttribute;
import aa.repository.ServiceProviderRepository;
import aa.saml.CredentialService;
import aa.service.AttributeAggregatorService;
import org.joda.time.DateTime;
import org.opensaml.Configuration;
import org.opensaml.DefaultBootstrap;
import org.opensaml.saml2.core.*;
import org.opensaml.saml2.core.impl.ResponseBuilder;
import org.opensaml.ws.soap.common.AbstractExtensibleSOAPObject;
import org.opensaml.ws.soap.soap11.Body;
import org.opensaml.ws.soap.soap11.Envelope;
import org.opensaml.xml.ConfigurationException;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.XMLObjectBuilderFactory;
import org.opensaml.xml.io.*;
import org.opensaml.xml.parse.StaticBasicParserPool;
import org.opensaml.xml.parse.XMLParserException;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSStringBuilder;
import org.opensaml.xml.security.SecurityException;
import org.opensaml.xml.security.SecurityHelper;
import org.opensaml.xml.security.credential.Credential;
import org.opensaml.xml.signature.Signature;
import org.opensaml.xml.signature.SignatureException;
import org.opensaml.xml.signature.Signer;
import org.opensaml.xml.util.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static aa.util.StreamUtils.singletonCollector;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping(headers = {"Content-Type=application/soap+xml"})
public class SAMLAttributeQueryController {

  private final static Logger LOG = LoggerFactory.getLogger(SAMLAttributeQueryController.class);

  private final String entityId;
  private final ServiceProviderRepository serviceProviderRepository;
  private final AttributeAggregatorService attributeAggregatorService;
  private final StaticBasicParserPool parserPool;
  private final CredentialService credentialService;

  @Autowired
  public SAMLAttributeQueryController(@Value("${aa.entityId}") String entityId,
                                      ServiceProviderRepository serviceProviderRepository,
                                      AttributeAggregatorService attributeAggregatorService,
                                      CredentialService credentialService) throws ConfigurationException, XMLParserException {
    DefaultBootstrap.bootstrap();
    this.entityId = entityId;
    this.serviceProviderRepository = serviceProviderRepository;
    this.attributeAggregatorService = attributeAggregatorService;
    this.credentialService = credentialService;

    this.parserPool = new StaticBasicParserPool();
    parserPool.setNamespaceAware(true);
    parserPool.initialize();
  }

  @RequestMapping(method = RequestMethod.POST, value = "/v1/query")
  public String attributeQuery(@RequestBody String soap) throws XMLParserException, MarshallingException, UnmarshallingException, SignatureException, SecurityException {
    AttributeQuery attributeQuery = getAttributeQuery(soap);

    String serviceProviderEntityId = attributeQuery.getIssuer().getValue();

    Optional<ServiceProvider> sp = serviceProviderRepository.findByEntityId(serviceProviderEntityId);
    if (!sp.isPresent()) {
      throw new SchemaNotFoundException("ServiceProvider " + serviceProviderEntityId + " has no attribute aggregations configured.");
    }

    String eppn = attributeQuery.getSubject().getNameID().getValue();
    List<UserAttribute> input = getUserAttributes(eppn);
    List<UserAttribute> userAttributes = attributeAggregatorService.aggregate(sp.get(), input);

    String soapResult = getSoapResult(userAttributes, eppn, serviceProviderEntityId);

    LOG.debug("Returning attribute query result {} for SP {}", soapResult, serviceProviderEntityId);

    return soapResult;
  }

  private AttributeQuery getAttributeQuery(String soap) throws XMLParserException, UnmarshallingException {
    Document document = parserPool.parse(new StringReader(soap));
    Element element = document.getDocumentElement();

    UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
    Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);

    AbstractExtensibleSOAPObject body = (AbstractExtensibleSOAPObject) ((Envelope) unmarshaller.unmarshall(element)).getBody();
    List<XMLObject> children = body.getOrderedChildren();
    return (AttributeQuery) children.stream().filter(xmlObject -> xmlObject instanceof AttributeQuery).collect(singletonCollector());
  }

  //very unfriendly API - but for signing we need the SAML2 objects
  private String getSoapResult(List<UserAttribute> userAttributes, String eppn, String serviceProviderEntityId) throws MarshallingException, SignatureException, SecurityException {
    XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();

    ResponseBuilder responseBuilder = (ResponseBuilder) builderFactory.getBuilder(Response.DEFAULT_ELEMENT_NAME);
    Response response = responseBuilder.buildObject();
    response.setIssuer(createIssuer());

    Status status = buildSAMLObject(Status.class, Status.DEFAULT_ELEMENT_NAME);
    StatusCode statusCode = buildSAMLObject(StatusCode.class, StatusCode.DEFAULT_ELEMENT_NAME);
    statusCode.setValue("urn:oasis:names:tc:SAML:2.0:status:Success");
    status.setStatusCode(statusCode);
    response.setStatus(status);

    Assertion assertion = buildSAMLObject(Assertion.class, Assertion.DEFAULT_ELEMENT_NAME);
    assertion.setIssuer(createIssuer());

    Subject subject = buildSAMLObject(Subject.class, Subject.DEFAULT_ELEMENT_NAME);
    NameID nameID = buildSAMLObject(NameID.class, NameID.DEFAULT_ELEMENT_NAME);
    nameID.setValue(eppn);
    subject.setNameID(nameID);
    assertion.setSubject(subject);

    Conditions conditions = buildSAMLObject(Conditions.class, Conditions.DEFAULT_ELEMENT_NAME);
    DateTime now = DateTime.now();
    conditions.setNotBefore(now);
    conditions.setNotOnOrAfter(now.plusDays(1));
    AudienceRestriction audienceRestriction = buildSAMLObject(AudienceRestriction.class, AudienceRestriction.DEFAULT_ELEMENT_NAME);
    Audience audience = buildSAMLObject(Audience.class, Audience.DEFAULT_ELEMENT_NAME);
    audience.setAudienceURI(serviceProviderEntityId);
    audienceRestriction.getAudiences().add(audience);
    conditions.getAudienceRestrictions().add(audienceRestriction);
    assertion.setConditions(conditions);

    AttributeStatement attributeStatement = buildSAMLObject(AttributeStatement.class, AttributeStatement.DEFAULT_ELEMENT_NAME);
    List<Attribute> attributes = attributeStatement.getAttributes();

    Map<String, List<UserAttribute>> grouped = userAttributes.stream().collect(Collectors.groupingBy(UserAttribute::getName));
    grouped.entrySet().forEach(entry -> attributes.add(createAttribute(entry.getKey(), entry.getValue().stream()
        .map(UserAttribute::getValues).flatMap(List::stream).collect(toList()))));

    assertion.getAttributeStatements().add(attributeStatement);
    response.getAssertions().add(assertion);

    return wrapSOAP(response);
  }

  private Issuer createIssuer() {
    Issuer issuer = buildSAMLObject(Issuer.class, Issuer.DEFAULT_ELEMENT_NAME);
    issuer.setValue(entityId);
    return issuer;
  }

  private Attribute createAttribute(String name, List<String> values) {
    Attribute attribute = buildSAMLObject(Attribute.class, Attribute.DEFAULT_ELEMENT_NAME);
    attribute.setName(name);
    attribute.getAttributeValues().addAll(getAttributeValues(values));
    return attribute;
  }

  private Collection<? extends XMLObject> getAttributeValues(List<String> values) {
    return values.stream().map(this::attributeValue).collect(toList());
  }

  private XSString attributeValue(String value) {
    XSStringBuilder stringBuilder = new XSStringBuilder();
    XSString attributeValue = stringBuilder.buildObject(AttributeValue.DEFAULT_ELEMENT_NAME, XSString.TYPE_NAME);
    attributeValue.setValue(value);
    return attributeValue;
  }


  @SuppressWarnings("unchecked")
  private String wrapSOAP(Response response) throws MarshallingException, SignatureException, SecurityException {
    Envelope envelope = buildSAMLObject(Envelope.class, Envelope.DEFAULT_ELEMENT_NAME);
    Body body = buildSAMLObject(Body.class, Body.DEFAULT_ELEMENT_NAME);

    body.getUnknownXMLObjects().add(response);

    envelope.setBody(body);

    MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
    Marshaller marshaller = marshallerFactory.getMarshaller(envelope);

    Signature signature = buildSAMLObject(Signature.class, Signature.DEFAULT_ELEMENT_NAME);

    Credential credential = credentialService.getCredential();
    signature.setSigningCredential(credential);

    SecurityHelper.prepareSignatureParams(signature, credential, Configuration.getGlobalSecurityConfiguration(), null);

    response.setSignature(signature);

    Element envelopeElem = marshaller.marshall(envelope);

    Signer.signObject(signature);

    StringWriter writer = new StringWriter();
    XMLHelper.writeNode(envelopeElem, writer);
    return writer.toString();
  }

  @SuppressWarnings({"unused", "unchecked"})
  private <T> T buildSAMLObject(final Class<T> objectClass, QName qName) {
    XMLObjectBuilderFactory builderFactory = Configuration.getBuilderFactory();
    return (T) builderFactory.getBuilder(qName).buildObject(qName);
  }

  private List<UserAttribute> getUserAttributes(String eppn) {
    return Collections.singletonList(
        new UserAttribute("urn:mace:dir:attribute-def:eduPersonPrincipalName", singletonList(eppn)));
  }

}
