package aa.serviceregistry;

import org.springframework.core.io.Resource;

import java.util.List;

public class TestingServiceRegistry extends ClassPathResourceServiceRegistry {

  public TestingServiceRegistry() {
    super(true);
  }

  @Override
  protected List<Resource> getResources() {
    return doGetResources("service-registry/saml20-sp-remote.test.json");
  }

}
