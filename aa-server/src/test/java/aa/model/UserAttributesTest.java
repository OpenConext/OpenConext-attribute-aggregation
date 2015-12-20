package aa.model;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class UserAttributesTest {

  @Test
  public void testToString() throws Exception {
    UserAttributes subject = new UserAttributes();
    subject.setServiceProviderEntityId("http://mock-idp");
    subject.setAttributes(Collections.singletonList(new UserAttribute("name", Collections.singletonList("value"), "source")));

    String s = subject.toString();
    assertEquals("UserAttributes{serviceProviderEntityId='http://mock-idp', attributes=[UserAttribute{name='name', values=[value], source='source'}]}", s);
  }
}