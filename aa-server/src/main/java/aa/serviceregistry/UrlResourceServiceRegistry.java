package aa.serviceregistry;

import org.springframework.core.io.Resource;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.singletonList;
import static java.util.concurrent.Executors.newScheduledThreadPool;

public class UrlResourceServiceRegistry extends ClassPathResourceServiceRegistry {

  private final BasicAuthenticationUrlResource urlResource;

  private final RestTemplate restTemplate = new RestTemplate();
  private final int period;
  private final String spRemotePath;
  private ScheduledFuture<?> scheduledFuture;
  private boolean lastCallFailed = true;

  public UrlResourceServiceRegistry(
      String username,
      String password,
      String spRemotePath,
      int period) throws IOException {
    super(false);
    this.urlResource = new BasicAuthenticationUrlResource(spRemotePath, username, password);
    this.spRemotePath = spRemotePath;
    this.period = period;

    SimpleClientHttpRequestFactory requestFactory = (SimpleClientHttpRequestFactory) restTemplate.getRequestFactory();
    requestFactory.setConnectTimeout(5 * 1000);

    schedule(period, TimeUnit.MINUTES);
    doInitializeMetadata(true);

    newScheduledThreadPool(1).scheduleAtFixedRate(this::initializeMetadata, period, period, TimeUnit.MINUTES);
    doInitializeMetadata(true);
  }

  private void schedule(int period, TimeUnit timeUnit) {
    if (this.scheduledFuture != null) {
      this.scheduledFuture.cancel(true);
    }
    this.scheduledFuture = newScheduledThreadPool(1).scheduleAtFixedRate(this::initializeMetadata, period, period, timeUnit);
  }

  @Override
  protected List<Resource> getResources() {
    LOG.debug("Fetching SP metadata entries from {}", spRemotePath);
    return singletonList(urlResource);
  }

  private void doInitializeMetadata(boolean forceRefresh) {
    try {
      if (forceRefresh ||  urlResource.isModified(period)) {
        super.initializeMetadata();
      } else {
        LOG.debug("Not refreshing SP metadata. Not modified");
      }
      //now maybe this is the first successful call after a failure, so check and change the period
      if (lastCallFailed) {
        schedule(period, TimeUnit.MINUTES);
      }
      lastCallFailed = false;
    } catch (Throwable e) {
      /*
       * By design we catch the error and not rethrow it.
       *
       * UrlResourceServiceRegistry has timing issues when the server reboots and required MetadataExporter endpoints
       * are not available yet. We re-schedule the timer to try every 5 seconds until it's succeeds
       */
      LOG.error("Error in refreshing / initializing metadata", e);
      lastCallFailed = true;
      schedule(5, TimeUnit.SECONDS);
    }
  }

  @Override
  protected void initializeMetadata() {
    doInitializeMetadata(false);
  }

}
