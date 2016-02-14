package aa.model;

import java.util.List;
import java.util.Objects;

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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserAttribute that = (UserAttribute) o;
    return skipConsent == that.skipConsent &&
        Objects.equals(name, that.name) &&
        Objects.equals(values, that.values) &&
        Objects.equals(source, that.source);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, values, source, skipConsent);
  }
}
