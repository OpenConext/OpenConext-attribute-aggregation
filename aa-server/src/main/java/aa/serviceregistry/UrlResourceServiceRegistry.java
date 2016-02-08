package aa.serviceregistry;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;
import static java.util.Collections.singletonList;
import static java.util.concurrent.Executors.newScheduledThreadPool;
import static org.springframework.http.HttpHeaders.IF_MODIFIED_SINCE;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;

public class UrlResourceServiceRegistry extends ClassPathResourceServiceRegistry {

  private final BasicAuthenticationUrlResource urlResource;

  private final RestTemplate restTemplate = new RestTemplate();
  private final int period;
  private final String spRemotePath;

  public UrlResourceServiceRegistry(
      String username,
      String password,
      String spRemotePath,
      int period) throws MalformedURLException {
    super(false);
    this.urlResource = new BasicAuthenticationUrlResource(spRemotePath, username, password);
    this.spRemotePath = spRemotePath;
    this.period = period;

    SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
    requestFactory.setConnectTimeout(5 * 1000);

    newScheduledThreadPool(1).scheduleAtFixedRate(this::initializeMetadata, period, period, TimeUnit.MINUTES);
    super.initializeMetadata();
  }

  @Override
  protected List<Resource> getResources() {
    LOG.debug("Fetching SP metadata entries from {}", spRemotePath);
    return singletonList(urlResource);
  }

  @Override
  protected void initializeMetadata()  {
    HttpHeaders headers = new HttpHeaders();
    String lastRefresh = RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")).minusMinutes(period));
    headers.set(IF_MODIFIED_SINCE, lastRefresh);

    ResponseEntity<String> result = restTemplate.exchange(spRemotePath, HEAD, new HttpEntity<>(headers), String.class);

    if (result.getStatusCode().equals(NOT_MODIFIED)) {
      LOG.debug("Not refreshing SP metadata. Not modified");
    } else {
      super.initializeMetadata();
    }
  }
}
