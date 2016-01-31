package aa.service;

import aa.aggregators.AttributeAggregator;
import aa.config.AuthorityConfiguration;
import aa.model.Attribute;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.ServiceProvider;
import aa.model.UserAttribute;
import aa.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static java.util.concurrent.Executors.newScheduledThreadPool;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;

public class AttributeAggregatorService {

  private final static Logger LOG = LoggerFactory.getLogger(AttributeAggregatorService.class);

  private final Map<String, AttributeAggregator> aggregators;
  private final Map<String, CachedAggregate> cache = new ConcurrentHashMap<>();
  private final ForkJoinPool forkJoinPool;
  private final AuthorityConfiguration configuration;
  private final long cacheDuration;

  public AttributeAggregatorService(List<AttributeAggregator> aggregators,
                                    AuthorityConfiguration configuration,
                                    long cacheDurationMilliseconds, long expiryIntervalCheckMilliseconds) {
    Assert.notEmpty(aggregators);
    this.aggregators = aggregators.stream().collect(toMap(AttributeAggregator::getAttributeAuthorityId, identity()));
    this.forkJoinPool = new ForkJoinPool(20 /* number of threads in embedded tomcat */ * aggregators.size());
    this.configuration = configuration;
    this.cacheDuration = cacheDurationMilliseconds;

    newScheduledThreadPool(1).scheduleAtFixedRate(this::clearExpiredAggregates, 0, expiryIntervalCheckMilliseconds, TimeUnit.MILLISECONDS);
  }

  public List<UserAttribute> aggregate(ServiceProvider serviceProvider, List<UserAttribute> input) {

    long start = System.currentTimeMillis();
    LOG.debug("Started to aggregate attributes for SP {} and input {}", serviceProvider, input);

    //all of the Attributes that this SP may receive
    List<Attribute> attributes = serviceProvider.getAggregations().stream().map(aggregation -> aggregation.getAttributes())
        .flatMap(s -> s.stream()).collect(toList());

    //all of the unique AttributeAuthorityConfigurations for the attributes
    Set<AttributeAuthorityConfiguration> authorityConfigurations = attributes.stream().map(attribute -> configuration.getAuthorityById(attribute.getAttributeAuthorityId())).collect(toSet());

    //all of the names of input UserAttributes that at least have one non-empty value
    List<String> inputNames = input.stream().filter(userAttribute -> userAttribute.getValues().stream().anyMatch(StringUtils::hasText))
        .map(UserAttribute::getName).collect(toList());

    //the actual AttributeAggregators to query filtered on the required input parameters
    List<AttributeAggregator> attributeAggregators = authorityConfigurations.stream()
        .map(attributeAuthority -> aggregators.get(attributeAuthority.getId()))
        .filter(attributeAggregator -> inputNames.containsAll(attributeAggregator.attributeKeysRequired()))
        .collect(toList());

    //all aggregatedAttributes
    List<UserAttribute> aggregatedAttributes;
    try {
      aggregatedAttributes = forkJoinPool.submit(() -> attributeAggregators.parallelStream().map(aggregator ->
          doAggregate(input, aggregator)).flatMap(List::stream).collect(toList())).get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException("Unable to schedule querying of attribute aggregators.", e);
    }

    //filter out those Attributes that are not allowed no return (rare case, but possible)
    List<UserAttribute> result = aggregatedAttributes.stream().filter(userAttribute -> allowedAttribute(attributes, userAttribute)).collect(toList());

    //finally mark the UserAttributes with skipConsent based on the Attribute
    result.forEach(userAttribute -> {
      Attribute attribute = attributes.stream().filter(
          attr -> attr.getAttributeAuthorityId().equals(userAttribute.getSource()) &&
          attr.getName().equals(userAttribute.getName()))
          .findAny()
          //this can not happen
          .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown UserAttribute %s returned for SP %s", userAttribute, serviceProvider)));
      userAttribute.setSkipConsent(attribute.isSkipConsent());
    });

    LOG.debug("Finished aggregating attributes in {} millis for SP {} and input {} with result {}",
        System.currentTimeMillis() - start, serviceProvider, input, result);

    return result;
  }

  private List<UserAttribute> doAggregate(List<UserAttribute> input, AttributeAggregator aggregator) {
    try {
      Optional<String> cacheKey = aggregator.cacheKey(input);
      CachedAggregate cachedAggregate = cacheKey.isPresent() ? cache.get(cacheKey.get()) : null;
      long now = System.currentTimeMillis();
      if (cachedAggregate != null && cachedAggregate.timestamp + cacheDuration > now) {
        LOG.debug("Returning userAttributes from cache {}", cachedAggregate.aggregate);
        return cachedAggregate.aggregate;
      }
      List<UserAttribute> userAttributes = aggregator.aggregate(input);
      if (cacheKey.isPresent() && userAttributes.size() > 0) {
        LOG.debug("Putting userAttributes in cache {} with key {}", userAttributes, cacheKey.get());
        cache.put(cacheKey.get(), new CachedAggregate(now, userAttributes));
      }
      return userAttributes;
    } catch (RuntimeException e) {
      LOG.warn("AttributeAggregator {} threw exception: {} ", aggregator, e);
      return Collections.<UserAttribute>emptyList();
    }
  }

  private boolean allowedAttribute(List<Attribute> attributes, UserAttribute userAttribute) {
    return attributes.stream().anyMatch(attribute -> userAttribute.getSource().equals(attribute.getAttributeAuthorityId()) &&
        userAttribute.getName().equals(attribute.getName()));
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

  private class CachedAggregate {
    long timestamp;
    List<UserAttribute> aggregate;

    public CachedAggregate(long timestamp, List<UserAttribute> aggregate) {
      this.timestamp = timestamp;
      this.aggregate = aggregate;
    }
  }

}
