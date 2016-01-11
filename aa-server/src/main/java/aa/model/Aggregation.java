package aa.model;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.EAGER;

@Entity(name = "aggregations")
public class Aggregation {

  @Id
  @GeneratedValue
  private Long id;

  @Column
  private String name;

  @ManyToMany(cascade = {PERSIST, REFRESH, DETACH, MERGE}, fetch = EAGER)
  @LazyCollection(LazyCollectionOption.FALSE)
  @JoinTable(
      name = "aggregations_service_providers",
      joinColumns = {@JoinColumn(name = "aggregation_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "service_provider_id", referencedColumnName = "id")})
  private Set<ServiceProvider> serviceProviders = new HashSet<>();

  @ManyToMany(cascade = {PERSIST, REFRESH, DETACH, MERGE}, fetch = EAGER)
  @LazyCollection(LazyCollectionOption.FALSE)
  @JoinTable(
      name = "aggregations_attributes",
      joinColumns = {@JoinColumn(name = "aggregation_id", referencedColumnName = "id")},
      inverseJoinColumns = {@JoinColumn(name = "attribute_id", referencedColumnName = "id")})
  private Set<Attribute> attributes = new HashSet<>();

  @Column
  private String userIdentifier;

  @Column
  private String userDisplayName;

  @Column(name = "ts")
  private Date created;

  public Aggregation() {
  }

  public Aggregation(Set<Attribute> attributes) {
    this.attributes = attributes;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Set<ServiceProvider> getServiceProviders() {
    return serviceProviders;
  }

  public void setServiceProviders(Set<ServiceProvider> serviceProviders) {
    this.serviceProviders = serviceProviders;
  }

  public Set<Attribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(Set<Attribute> attributes) {
    this.attributes = attributes;
  }

  public String getUserIdentifier() {
    return userIdentifier;
  }

  public void setUserIdentifier(String userIdentifier) {
    this.userIdentifier = userIdentifier;
  }

  public String getUserDisplayName() {
    return userDisplayName;
  }

  public void setUserDisplayName(String userDisplayName) {
    this.userDisplayName = userDisplayName;
  }

  public Date getCreated() {
    return created;
  }

  public void setCreated(Date created) {
    this.created = created;
  }

  @Override
  public String toString() {
    return "Aggregation{" +
        "id=" + getId() +
        ", name='" + getName() + '\'' +
        ", serviceProviders=" + getServiceProviders() +
        ", attributes=" + getAttributes() +
        ", userIdentifier='" + getUserIdentifier() + '\'' +
        ", userDisplayName='" + getUserDisplayName() + '\'' +
        ", created=" + getCreated() +
        '}';
  }
}
