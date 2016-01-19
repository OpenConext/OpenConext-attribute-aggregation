package aa.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Schema {

  private List<String> schemas = singletonList("urn:ietf:params:scim:schemas:core:2.0:Schema");
  private String description;
  private String id;
  private String mutability = "readOnly";
  private String name;

  private String returned = "default";

  private List<Attribute> attributes;

  public Schema() {
  }

  public Schema(String description, String id, String name, List<Attribute> attributes) {
    this.description = description;
    this.id = id;
    this.name = name;
    this.attributes = attributes;
  }

  public List<String> getSchemas() {
    return schemas;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getMutability() {
    return mutability;
  }

  public void setMutability(String mutability) {
    this.mutability = mutability;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getReturned() {
    return returned;
  }

  public void setReturned(String returned) {
    this.returned = returned;
  }

  public List<Attribute> getAttributes() {
    return attributes;
  }

  public void setAttributes(List<Attribute> attributes) {
    this.attributes = attributes;
  }

  @Override
  public String toString() {
    return "Schema{" +
        "description='" + description + '\'' +
        ", id='" + id + '\'' +
        ", name='" + name + '\'' +
        ", mutability='" + mutability + '\'' +
        ", returned='" + returned + '\'' +
        ", attributes=" + attributes +
        '}';
  }
}
