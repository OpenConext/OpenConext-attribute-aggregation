package aa.model;

import org.junit.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class UserAttributesTest {

  @Test
  public void testToString() throws Exception {
    UserAttributes subject = new UserAttributes();
    subject.setServiceProviderEntityId("http://mock-idp");
    subject.setAttributes(singletonList(createUserAttribute()));

    String s = subject.toString();
    assertEquals("UserAttributes{serviceProviderEntityId='http://mock-idp', attributes=[UserAttribute{name='name', values=[value1, value2], source='source', skipConsent=false}]}", s);
  }

  @Test
  public void testEquals() {
    List<UserAttribute> list = new ArrayList<>();
    list.add(createUserAttribute());
    list.add(createUserAttribute());
    assertEquals(list.get(0), list.get(1));

    Set<UserAttribute> set = new HashSet<>(list);
    assertEquals(1, set.size());
  }

  private UserAttribute createUserAttribute() {
    return new UserAttribute("name", asList("value1", "value2"), "source");
  }
}