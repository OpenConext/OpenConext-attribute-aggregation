package aa.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import java.util.Objects;

@Entity(name = "attributes")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Attribute {

  @Id
  @GeneratedValue
  private Long id;

  @Column(name = "attribute_authority_id")
  private String attributeAuthorityId;

  @Column
  private String name;

  @Transient
  private boolean caseExact;

  @Transient
  private String description;

  @Transient
  private boolean multiValued;

  @Transient
  private String mutability;

  @Transient
  private boolean required;

  @Transient
  private String returned;

  @Transient
  private String type;

  @Transient
  private String uniqueness;

  public Attribute() {
  }

  public Attribute(String name, String attributeAuthorityId) {
    this.name = name;
    this.attributeAuthorityId = attributeAuthorityId;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getAttributeAuthorityId() {
    return attributeAuthorityId;
  }

  public void setAttributeAuthorityId(String attributeAuthorityId) {
    this.attributeAuthorityId = attributeAuthorityId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean isCaseExact() {
    return caseExact;
  }

  public void setCaseExact(boolean caseExact) {
    this.caseExact = caseExact;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isMultiValued() {
    return multiValued;
  }

  public void setMultiValued(boolean multiValued) {
    this.multiValued = multiValued;
  }

  public String getMutability() {
    return mutability;
  }

  public void setMutability(String mutability) {
    this.mutability = mutability;
  }

  public boolean isRequired() {
    return required;
  }

  public void setRequired(boolean required) {
    this.required = required;
  }

  public String getReturned() {
    return returned;
  }

  public void setReturned(String returned) {
    this.returned = returned;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getUniqueness() {
    return uniqueness;
  }

  public void setUniqueness(String uniqueness) {
    this.uniqueness = uniqueness;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Attribute)) return false;
    Attribute attribute = (Attribute) o;
    return Objects.equals(attributeAuthorityId, attribute.attributeAuthorityId) &&
        Objects.equals(name, attribute.name);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeAuthorityId, name);
  }

  @Override
  public String toString() {
    return "Attribute{" +
        "id=" + getId() +
        ", attributeAuthorityId='" + getAttributeAuthorityId() + '\'' +
        ", name='" + getName() + '\'' +
        ", caseExact=" + isCaseExact() +
        ", description='" + getDescription() + '\'' +
        ", multiValued=" + isMultiValued() +
        ", mutability='" + getMutability() + '\'' +
        ", required=" + isRequired() +
        ", returned='" + getReturned() + '\'' +
        ", type='" + getType() + '\'' +
        ", uniqueness='" + getUniqueness() + '\'' +
        '}';
  }
}
