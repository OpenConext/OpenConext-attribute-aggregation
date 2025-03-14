package aa.cache;

import aa.model.UserAttribute;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.newScheduledThreadPool;

/**
 * Do not use in clustered environment when accessing the Attribute Aggregator through
 * a load balancer.
 */
public class SimpleInMemoryUserAttributeCache extends AbstractUserAttributeCache {

    private final Map<String, CachedAggregate> cache = new ConcurrentHashMap<>();

    public SimpleInMemoryUserAttributeCache(long cacheDurationMilliseconds, long clearExpiredAggregatesPeriod) {
        super(cacheDurationMilliseconds);
        if (cacheDurationMilliseconds > 0) {
            newScheduledThreadPool(1).scheduleAtFixedRate(this::clearExpiredAggregates, 0, clearExpiredAggregatesPeriod, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    protected List<UserAttribute> doGet(String cacheKey) {
        CachedAggregate cachedAggregate = cache.get(cacheKey);
        long now = System.currentTimeMillis();
        if (cachedAggregate != null && cachedAggregate.timestamp + getCacheDuration() > now) {
            return cachedAggregate.aggregate;
        }
        return null;
    }

    @Override
    protected void doPut(String cacheKey, List<UserAttribute> userAttributes) {
        long now = System.currentTimeMillis();
        cache.put(cacheKey, new CachedAggregate(now, userAttributes));
    }

    private void clearExpiredAggregates() {
        long now = System.currentTimeMillis();
        long cacheDuration = getCacheDuration();
        cache.forEach((key, aggregate) -> {
            if (aggregate.timestamp + cacheDuration < now) {
                LOG.debug("Removing expired aggregation with key {}", key);
                cache.remove(key);
            }
        });
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
