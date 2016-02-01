package aa.serviceregistry;

import aa.model.ServiceProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class ClassPathResourceServiceRegistry implements ServiceRegistry {

  protected final static Logger LOG = LoggerFactory.getLogger(ClassPathResourceServiceRegistry.class);

  private final static ObjectMapper objectMapper = new ObjectMapper();

  private Map<String, ServiceProvider> entityMetaData = new ConcurrentHashMap<>();

  public ClassPathResourceServiceRegistry(boolean initialize) {
    //this provides subclasses a hook to set properties before initializing metadata
    if (initialize) {
      initializeMetadata();
    }
  }

  protected void initializeMetadata() {
    List<Resource> resources = getResources();
    Map<String, ServiceProvider> serviceProviderMap = resources.stream().map(this::parseEntities).flatMap(m -> m.entrySet().stream()).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    entityMetaData.putAll(serviceProviderMap);
  }

  protected List<Resource> getResources() {
    return doGetResources("service-registry/saml20-sp-remote.json", "service-registry/saml20-sp-remote.test.json");
  }

  protected List<Resource> doGetResources(String... paths) {
    return asList(paths).stream().map(ClassPathResource::new).collect(toList());
  }

  @Override
  public Collection<ServiceProvider> serviceProviders() {
    return entityMetaData.values();
  }

  @Override
  public Optional<ServiceProvider> serviceProviderByEntityId(String entityId) {
    ServiceProvider sp = this.entityMetaData.get(entityId);
    return sp == null ? Optional.empty() : Optional.of(sp);
  }

  protected Map<String, ServiceProvider> parseEntities(Resource resource) {
    List<Map<String, Object>> list = readValue(resource);
    return list.stream().map(entry ->
        new ServiceProvider(
            (String) entry.get("entityid"),
            getMetaDateEntry(entry, "description"),
            getMetaDateEntry(entry, "name"))
    ).sorted(sortEntityMetaData()).collect(toMap(ServiceProvider::getEntityId, e -> e));
  }

  @SuppressWarnings("unchecked")
  private List<Map<String, Object>> readValue(Resource resource) {
    try {
      return objectMapper.readValue(resource.getInputStream(), List.class);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Comparator<? super ServiceProvider> sortEntityMetaData() {
    return (e1, e2) ->
        e1.getName() == null ? -1 : e2.getName() == null ? -1 : e1.getName().trim().compareTo(e2.getName().trim());
  }

  @SuppressWarnings("unchecked")
  private String getMetaDateEntry(Map<String, Object> entry, String attributeName) {
    String attr = null;
    Map<String, String> attributes = (Map<String, String>) entry.get(attributeName);
    if (attributes != null) {
      attr = attributes.get("en");
      if (attr == null) {
        // try the other language
        attr = attributes.get("nl");
      }
    }
    return attr;
  }

}
