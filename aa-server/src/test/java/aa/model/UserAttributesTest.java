package aa.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
  public void testHashCode() {
    List<UserAttribute> list = new ArrayList<>();
    list.add(createUserAttribute());
    list.add(createUserAttribute());
    assertEquals(list.get(0), list.get(1));

    Set<UserAttribute> set = new HashSet<>(list);
    assertEquals(1, set.size());
  }

  @Test
  public void testEquals() {
    UserAttribute userAttribute = new UserAttribute("name", singletonList("value"), "source");
    assertNotEquals(userAttribute, new UserAttribute("name1", singletonList("value"), "source"));
    assertNotEquals(userAttribute, new UserAttribute("name", singletonList("value1"), "source"));
    assertNotEquals(userAttribute, new UserAttribute("name", singletonList("value"), "source1"));
  }

  private UserAttribute createUserAttribute() {
    return new UserAttribute("name", asList("value1", "value2"), "source");
  }
}