package aa.cache;

import aa.model.UserAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;

/**
 * Do not use in clustered environment when accessing the Attribute Aggregator through
 * a load balancer.
 */
public class SimpleInMemoryUserAttributeCache implements UserAttributeCache {

  private final static Logger LOG = LoggerFactory.getLogger(SimpleInMemoryUserAttributeCache.class);

  private final Map<String, CachedAggregate> cache = new ConcurrentHashMap<>();

  private final long cacheDuration;

  public SimpleInMemoryUserAttributeCache(long cacheDurationMilliseconds, long clearExpiredAggregatesPeriod) {
    this.cacheDuration = cacheDurationMilliseconds;
    Assert.isTrue(cacheDurationMilliseconds > 0);
    newScheduledThreadPool(1).scheduleAtFixedRate(this::clearExpiredAggregates, 0, clearExpiredAggregatesPeriod, TimeUnit.MILLISECONDS);
  }

  private void clearExpiredAggregates() {
    long now = System.currentTimeMillis();
    cache.forEach((key, aggregate) -> {
      if (aggregate.timestamp + cacheDuration < now) {
        LOG.debug("Removing expired aggregation with key {}", key);
        cache.remove(key);
      }
    });
  }

  @Override
  public Optional<List<UserAttribute>> get(Optional<String> cacheKey) {
    if (!cacheKey.isPresent()) {
      return Optional.empty();
    }
    CachedAggregate cachedAggregate = cache.get(cacheKey.get());
    long now = System.currentTimeMillis();
    if (cachedAggregate != null && cachedAggregate.timestamp + cacheDuration > now) {
      LOG.debug("Returning userAttributes from cache {}", cachedAggregate.aggregate);
      return Optional.of(cachedAggregate.aggregate);
    }
    return Optional.empty();
  }

  @Override
  public void put(Optional<String> cacheKey, List<UserAttribute> userAttributes) {
    if (cacheKey.isPresent() && !CollectionUtils.isEmpty(userAttributes)) {
      LOG.debug("Putting userAttributes in cache {} with key {}", userAttributes, cacheKey.get());
      long now = System.currentTimeMillis();
      cache.put(cacheKey.get(), new CachedAggregate(now, userAttributes));
    }
  }

  private class CachedAggregate {
    long timestamp;
    List<UserAttribute> aggregate;

    public CachedAggregate(long timestamp, List<UserAttribute> aggregate) {
      this.timestamp = timestamp;
      this.aggregate = aggregate;
    }
  }
}
