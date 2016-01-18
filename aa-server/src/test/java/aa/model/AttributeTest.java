package aa.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

public class AttributeTest {

  @Test
  public void testEquals() throws Exception {
    Attribute attribute = new Attribute("name","aa1");
    Attribute other = new Attribute("name","aa1");
    assertEquals(attribute, other);

    Set<Attribute> attributes = new HashSet<>(Arrays.asList(attribute, other));
    assertEquals(1, attributes.size());
  }

  @Test
  public void testClone() throws Exception {
    Attribute attribute = new Attribute("name","aa1");
    Attribute cloned = (Attribute) attribute.clone();

    assertFalse(cloned == attribute);
    assertEquals(cloned, attribute);
  }
}