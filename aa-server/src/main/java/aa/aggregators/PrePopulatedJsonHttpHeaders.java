package aa.aggregators;

import org.springframework.http.HttpHeaders;

public class PrePopulatedJsonHttpHeaders extends HttpHeaders {

  public PrePopulatedJsonHttpHeaders() {
    super();
    this.add(HttpHeaders.CONTENT_TYPE, "application/json");
  }
}
