package aa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity(name = "service_providers")
public class ServiceProvider {

  @Id
  @GeneratedValue
  private Long id;

  @Column(name = "entity_id")
  private String entityId;

  @Transient
  private String name;

  @Transient
  private String description;

  @Transient
  private boolean attributeAggregationRequired;

  @ManyToMany(mappedBy = "serviceProviders", fetch = FetchType.EAGER)
  @JsonIgnore
  private Set<Aggregation> aggregations = new HashSet<>();

  public ServiceProvider(String entityId) {
    this.entityId = entityId;
  }

  public ServiceProvider() {
  }

  public ServiceProvider(String entityId, String description, String name, boolean attributeAggregationRequired) {
    this.entityId = entityId;
    this.description = description;
    this.name = name;
    this.attributeAggregationRequired = attributeAggregationRequired;
  }

  public ServiceProvider(Set<Aggregation> aggregations) {
    this.aggregations = aggregations;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEntityId() {
    return entityId;
  }

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<Aggregation> getAggregations() {
    return aggregations;
  }

  public void setAggregations(Set<Aggregation> aggregations) {
    this.aggregations = aggregations;
  }

  public boolean isAttributeAggregationRequired() {
    return attributeAggregationRequired;
  }

  public void setAttributeAggregationRequired(boolean attributeAggregationRequired) {
    this.attributeAggregationRequired = attributeAggregationRequired;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ServiceProvider)) return false;
    ServiceProvider that = (ServiceProvider) o;
    return Objects.equals(entityId, that.entityId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(entityId);
  }

  @Override
  public String toString() {
    return "ServiceProvider{" +
        "entityId='" + getEntityId() + '\'' +
        '}';
  }
}
