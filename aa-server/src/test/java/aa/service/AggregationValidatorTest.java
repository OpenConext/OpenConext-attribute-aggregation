package aa.service;

import aa.config.AuthorityConfiguration;
import aa.model.*;
import aa.serviceregistry.ClassPathResourceServiceRegistry;
import aa.serviceregistry.ServiceRegistry;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;

import static java.util.Arrays.asList;

public class AggregationValidatorTest {

  private static ServiceRegistry serviceRegistry;

  @BeforeClass
  public static void beforeClass() throws IOException {
    serviceRegistry = new ClassPathResourceServiceRegistry(true);
  }

  private AuthorityConfiguration authorityConfiguration = new AuthorityConfiguration(asList(
      new AttributeAuthorityConfiguration("aa1", asList(
          new Attribute("name1", "aa1"),
          new Attribute("name2", "aa1")
      )),
      new AttributeAuthorityConfiguration("aa2", asList(
          new Attribute("name3", "aa2")
      ))
  ));

  private AggregationValidator subject = new AggregationValidator();

  @Test
  public void testValid() throws Exception {
    Aggregation aggregation = validAggregation();
    subject.validate(authorityConfiguration, serviceRegistry, aggregation);
  }

  @Test(expected = UnknownServiceProviderException.class)
  public void testNoValidServiceProvider() throws Exception {
    Aggregation aggregation = validAggregation();
    aggregation.setServiceProviders(new HashSet<>(asList(new ServiceProvider("http://unknown-sp"))));
    subject.validate(authorityConfiguration, serviceRegistry, aggregation);
  }

  @Test(expected = UnknownAttributeException.class)
  public void testNoValidAttributeName() throws Exception {
    Aggregation aggregation = validAggregation();
    aggregation.setAttributes(new HashSet<>(asList(new Attribute("unknown","aa1"))));
    subject.validate(authorityConfiguration, serviceRegistry, aggregation);
  }

  @Test(expected = UnknownAttributeException.class)
  public void testNoValidAttributeAuthorityId() throws Exception {
    Aggregation aggregation = validAggregation();
    aggregation.setAttributes(new HashSet<>(asList(new Attribute("name1","unknown"))));
    subject.validate(authorityConfiguration, serviceRegistry, aggregation);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoServiceProviders() throws Exception {
    Aggregation aggregation = validAggregation();
    aggregation.setServiceProviders(new HashSet<>());
    subject.validate(authorityConfiguration, serviceRegistry, aggregation);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoValidAttributes() throws Exception {
    Aggregation aggregation = validAggregation();
    aggregation.setAttributes(new HashSet<>());
    subject.validate(authorityConfiguration, serviceRegistry, aggregation);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoValidName() throws Exception {
    Aggregation aggregation = validAggregation();
    aggregation.setName(null);
    subject.validate(authorityConfiguration, serviceRegistry, aggregation);
  }

  private Aggregation validAggregation() {
    Aggregation aggregation = new Aggregation();
    aggregation.setName("name");
    aggregation.setServiceProviders(new HashSet<>(asList(new ServiceProvider("http://mock-sp"))));
    aggregation.setAttributes(new HashSet<>(asList(new Attribute("name1","aa1"))));
    return aggregation;

  }

}