package aa.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;

public class AttributeTest {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  public void testClone() throws Exception {
    String content = IOUtils.toString(new ClassPathResource("json/client/attribute.json").getInputStream());
    Attribute attribute = objectMapper.readValue(content, Attribute.class);
    Attribute cloned = (Attribute) attribute.clone();

    assertFalse(cloned == attribute);
    assertEquals(attribute.toString(), cloned.toString());
  }
}