package aa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Objects;

public class AttributeAuthorityConfiguration {

  private String id;
  private String description;
  private String endpoint;
  private String user;
  @JsonIgnore
  private String password;
  private List<Attribute> attributes;
  private List<RequiredInputAttribute> requiredInputAttributes;
  private int timeOut;

  public AttributeAuthorityConfiguration() {
  }

  public AttributeAuthorityConfiguration(String id) {
    this.id = id;
  }

  public AttributeAuthorityConfiguration(String id, List<Attribute> attributes) {
    this.id = id;
    this.attributes = attributes;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getUser() {
    return user;
  }

  public void setUser(String user) {
    this.user = user;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  public int getTimeOut() {
    return timeOut;
  }

  public void setTimeOut(int timeOut) {
    this.timeOut = timeOut;
  }

  public List<RequiredInputAttribute> getRequiredInputAttributes() {
    return requiredInputAttributes;
  }

  public void setRequiredInputAttributes(List<RequiredInputAttribute> requiredInputAttributes) {
    this.requiredInputAttributes = requiredInputAttributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AttributeAuthorityConfiguration)) return false;
    AttributeAuthorityConfiguration that = (AttributeAuthorityConfiguration) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return "AttributeAuthority{" +
        "timeOut=" + getTimeOut() +
        ", attributes=" + getAttributes() +
        ", user='" + getUser() + '\'' +
        ", endpoint='" + getEndpoint() + '\'' +
        ", description='" + getDescription() + '\'' +
        ", id='" + getId() + '\'' +
        '}';
  }
}
