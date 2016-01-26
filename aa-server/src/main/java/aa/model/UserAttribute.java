package aa.model;

import java.util.List;

public class UserAttribute {

  private String name;
  private List<String> values;
  private String source;
  private boolean skipConsent;

  public UserAttribute() {
  }

  public UserAttribute(String name, List<String> values) {
    this.name = name;
    this.values = values;
  }

  public UserAttribute(String name, List<String> values, String source) {
    this.name = name;
    this.values = values;
    this.source = source;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<String> getValues() {
    return values;
  }

  public void setValues(List<String> values) {
    this.values = values;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public boolean isSkipConsent() {
    return skipConsent;
  }

  public void setSkipConsent(boolean skipConsent) {
    this.skipConsent = skipConsent;
  }

  @Override
  public String toString() {
    return "UserAttribute{" +
        "name='" + name + '\'' +
        ", values=" + values +
        ", source='" + source + '\'' +
        ", skipConsent=" + skipConsent +
        '}';
  }
}
