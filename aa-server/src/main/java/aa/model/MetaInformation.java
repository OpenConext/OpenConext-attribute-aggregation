package aa.model;

public class MetaInformation {

  private String created;
  private String lastModified;
  private String location;
  private String resourceType;
  private String version = "W/\"3694e05e9dff594\"";

  public MetaInformation() {
  }

  public MetaInformation(String created, String lastModified, String location, String resourceType) {
    this.created = created;
    this.lastModified = lastModified;
    this.location = location;
    this.resourceType = resourceType;
  }

  public String getCreated() {
    return created;
  }

  public void setCreated(String created) {
    this.created = created;
  }

  public String getLastModified() {
    return lastModified;
  }

  public void setLastModified(String lastModified) {
    this.lastModified = lastModified;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getResourceType() {
    return resourceType;
  }

  public void setResourceType(String resourceType) {
    this.resourceType = resourceType;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }
}
