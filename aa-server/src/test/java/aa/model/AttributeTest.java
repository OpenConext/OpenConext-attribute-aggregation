package aa.model;

import org.eclipse.jetty.server.session.HashSessionIdManager;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class AttributeTest {

  @Test
  public void testEquals() throws Exception {
    Attribute attribute = new Attribute("name","aa1");
    Attribute other = new Attribute("name","aa1");
    assertEquals(attribute, other);

    Set<Attribute> attributes = new HashSet<>(Arrays.asList(attribute, other));
    assertEquals(1, attributes.size());
  }
}