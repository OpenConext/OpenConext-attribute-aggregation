package aa.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

import static java.util.Collections.singletonList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceType {

  private List<String> schemas = singletonList("urn:ietf:params:scim:schemas:core:2.0:Schema");
  private String description = "SCIM Attributes resource type for use with SURFconext Attribute Aggregation";
  private String endpoint = "/v2/Me";
  private String id;
  private String name;
  private String schema;
  private MetaInformation meta;

  public ResourceType() {
  }

  public ResourceType(String spEntityId, MetaInformation meta) {
    this.id = spEntityId;
    this.name = spEntityId;
    this.schema = "urn:scim:schemas:extension:surf:".concat(spEntityId);
    this.meta = meta;
  }

  public List<String> getSchemas() {
    return schemas;
  }

  public void setSchemas(List<String> schemas) {
    this.schemas = schemas;
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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSchema() {
    return schema;
  }

  public void setSchema(String schema) {
    this.schema = schema;
  }

  public MetaInformation getMeta() {
    return meta;
  }

  public void setMeta(MetaInformation meta) {
    this.meta = meta;
  }
}
