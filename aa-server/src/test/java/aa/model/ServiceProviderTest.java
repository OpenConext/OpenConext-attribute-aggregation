package aa.model;

import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class ServiceProviderTest {

  @Test
  public void testEquals() throws Exception {
    ServiceProvider serviceProvider = new ServiceProvider("http://mock-idp");
    ServiceProvider other = new ServiceProvider("http://mock-idp");
    assertEquals(serviceProvider, other);

    Set<ServiceProvider> attributes = new HashSet<>(Arrays.asList(serviceProvider, other));
    assertEquals(1, attributes.size());

  }
}