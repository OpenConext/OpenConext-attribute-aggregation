package aa.web;

import org.junit.Test;

import java.util.regex.Pattern;

import static junit.framework.TestCase.*;

public class ApacheConfTest {

  private final Pattern shibProtected = Pattern.compile("/aa/api/internal/(.*)");

  @Test
  public void testPattern() throws Exception {
    assertTrue(matches("/aa/api/internal/aggregations"));

    assertFalse(matches("/aa/api/public/git"));
    assertFalse(matches("/aa/api/v1/Schema"));
    assertFalse(matches("/aa/api/attribute/aggregate"));
    assertFalse(matches("/aa/api/t/aggregate"));
  }

  private boolean matches(String url) {
    return shibProtected.matcher(url).matches();
  }
}
