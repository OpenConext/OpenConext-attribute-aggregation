package aa.model;

public class MetaInformation {

  private String created;
  private String lastModified;
  private String location;
  private String resourceType;
  private String version;

  public MetaInformation(String created, String lastModified, String location, String resourceType, String version) {
    this.created = created;
    this.lastModified = lastModified;
    this.location = location;
    this.resourceType = resourceType;
    this.version = version;
  }

  public String getCreated() {
    return created;
  }

  public String getLastModified() {
    return lastModified;
  }

  public String getLocation() {
    return location;
  }

  public String getResourceType() {
    return resourceType;
  }

  public String getVersion() {
    return version;
  }
}
