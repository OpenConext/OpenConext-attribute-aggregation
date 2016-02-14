package aa.serviceregistry;

import aa.model.ServiceProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.stream.Collectors.toMap;

public class ClassPathResourceServiceRegistry implements ServiceRegistry {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  private final static ObjectMapper objectMapper = new ObjectMapper();

  private Map<String, ServiceProvider> entityMetaData = new ConcurrentHashMap<>();

  public ClassPathResourceServiceRegistry(boolean initialize) throws IOException {
    //this provides subclasses a hook to set properties before initializing metadata
    if (initialize) {
      initializeMetadata();
    }
  }

  protected void initializeMetadata() throws IOException {
    long start = System.currentTimeMillis();
    LOG.debug("Starting refreshing SP metadata.");
    List<Resource> resources = getResources();
    Map<String, ServiceProvider> serviceProviderMap = resources.stream().map(this::parseEntities).flatMap(m -> m.entrySet().stream()).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    entityMetaData.putAll(serviceProviderMap);
    LOG.debug("Finished refreshing SP metadata in {} ms.", System.currentTimeMillis() - start);
  }

  protected List<Resource> getResources() {
    return Collections.singletonList(new ClassPathResource("service-registry/service-providers.json"));
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
            getMetaDateEntry(entry, "name"),
            getAttributeAggregationRequired(entry))
    ).sorted(sortEntityMetaData()).collect(toMap(ServiceProvider::getEntityId, e -> e));
  }

  private boolean getAttributeAggregationRequired(Map<String, Object> entry) {
    String attributeAggregationRequired = (String) entry.get("coin:attribute_aggregation_required");
    if (StringUtils.hasText(attributeAggregationRequired)) {
      return attributeAggregationRequired.equals("1");
    }
    return false;
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
    String attribute = (String) entry.get(attributeName + ":en");
    if (attribute == null) {
      // try the other language
      attribute = (String) entry.get(attributeName + ":nl");
    }
    return attribute;
  }

}
