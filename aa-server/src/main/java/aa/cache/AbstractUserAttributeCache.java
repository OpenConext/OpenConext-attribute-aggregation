package aa.cache;

import aa.model.UserAttribute;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public abstract class AbstractUserAttributeCache implements UserAttributeCache {

  protected final Logger LOG = LoggerFactory.getLogger(getClass());

  private final long cacheDuration;

  public AbstractUserAttributeCache(long cacheDurationMilliseconds) {
    this.cacheDuration = cacheDurationMilliseconds;
    Assert.isTrue(cacheDurationMilliseconds > 0);
  }

  @Override
  public Optional<List<UserAttribute>> get(Optional<String> cacheKey) throws IOException {
    if (!cacheKey.isPresent()) {
      return Optional.empty();
    }
    List<UserAttribute> userAttributes = this.doGet(cacheKey.get());
    LOG.debug("Returning userAttributes from cache {}", userAttributes);
    return userAttributes != null ? Optional.of(userAttributes) : Optional.empty();
  }

  //may return null as only used internally
  protected abstract List<UserAttribute> doGet(String cacheKey) throws IOException;

  @Override
  public void put(Optional<String> cacheKey, List<UserAttribute> userAttributes) throws IOException {
    if (cacheKey.isPresent() && !CollectionUtils.isEmpty(userAttributes)) {
      LOG.debug("Putting userAttributes in cache {} with key {}", userAttributes, cacheKey.get());
      this.doPut(cacheKey.get(), userAttributes);
    }
  }

  //may return null as only used internally
  protected abstract void doPut(String cacheKey, List<UserAttribute> userAttributes) throws JsonProcessingException;

  public long getCacheDuration() {
    return cacheDuration;
  }
}
