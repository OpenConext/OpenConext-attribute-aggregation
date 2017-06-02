package aa.service;

import aa.aggregators.AttributeAggregator;
import aa.cache.UserAttributeCache;
import aa.config.AuthorityConfiguration;
import aa.model.Aggregation;
import aa.model.ArpAggregationRequest;
import aa.model.ArpValue;
import aa.model.Attribute;
import aa.model.AttributeAuthorityConfiguration;
import aa.model.ServiceProvider;
import aa.model.UserAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
public class AttributeAggregatorService {

    private final static Logger LOG = LoggerFactory.getLogger(AttributeAggregatorService.class);

    private final Map<String, AttributeAggregator> aggregators;
    private final ForkJoinPool forkJoinPool;
    private final AuthorityConfiguration configuration;
    private final UserAttributeCache cache;

    public AttributeAggregatorService(List<AttributeAggregator> aggregators,
                                      AuthorityConfiguration configuration,
                                      UserAttributeCache cache) {
        Assert.notEmpty(aggregators, "Aggregators must not be empty");
        this.aggregators = aggregators.stream().collect(toMap(AttributeAggregator::getAttributeAuthorityId, identity()));
        this.forkJoinPool = new ForkJoinPool(20 /* number of threads in embedded tomcat */ * aggregators.size());
        this.configuration = configuration;
        this.cache = cache;
    }

    public List<UserAttribute> aggregate(ServiceProvider serviceProvider, List<UserAttribute> input) {

        long start = System.currentTimeMillis();
        LOG.debug("Started to aggregate attributes for SP {} and input {}", serviceProvider, input);

        //all of the Attributes that this SP may receive
        List<Attribute> attributes = serviceProvider.getAggregations().stream().map(Aggregation::getAttributes)
            .flatMap(Collection::stream).collect(toList());

        //all of the unique AttributeAuthorityConfigurations for the attributes
        Set<AttributeAuthorityConfiguration> authorityConfigurations = attributes.stream()
            .map(attribute -> configuration.getAuthorityById(attribute.getAttributeAuthorityId())).collect(toSet());

        List<UserAttribute> aggregatedAttributes = getUserAttributes(input, authorityConfigurations);

        //filter out those Attributes that are not allowed no return (rare case, but possible)
        List<UserAttribute> result = aggregatedAttributes.stream()
            .filter(userAttribute -> allowedAttribute(attributes, userAttribute)).collect(toList());

        //finally mark the UserAttributes with skipConsent based on the Attribute
        result.forEach(userAttribute -> {
            Attribute attribute = attributes.stream().filter(
                attr -> attr.getAttributeAuthorityId().equals(userAttribute.getSource()) &&
                    attr.getName().equals(userAttribute.getName()))
                .findAny()
                //this can not happen as those would already have been filtered out
                .orElseThrow(() -> new IllegalArgumentException(format("Unknown UserAttribute %s returned for SP %s", userAttribute, serviceProvider)));
            userAttribute.setSkipConsent(attribute.isSkipConsent());
        });

        LOG.debug("Finished aggregating attributes in {} millis for SP {} and input {} with result {}",
            System.currentTimeMillis() - start, serviceProvider, input, result);

        return result;
    }

    public List<UserAttribute> aggregateNoServiceCheck(List<UserAttribute> input) {
        long start = System.currentTimeMillis();
        LOG.debug("Started to aggregate attributes without Service check for input {}", input);

        //get attributes from all authorities
        List<UserAttribute> aggregatedAttributes = getUserAttributes(input, configuration.getAuthorities());

        LOG.debug("Finished aggregating attributes without Service check in {} millis for input {} with result {}",
            System.currentTimeMillis() - start, input, aggregatedAttributes);

        return aggregatedAttributes;
    }

    public List<UserAttribute> aggregateBasedOnArp(ArpAggregationRequest arpAggregationRequest) {
        long start = System.currentTimeMillis();
        LOG.debug("Started to aggregate attributes based on ARP for input {}", arpAggregationRequest);

        Set<String> sources = arpAggregationRequest.getArpAttributes().values().stream()
            .map(arpValues -> arpValues.stream().map(ArpValue::getSource))
            .flatMap(stringStream -> stringStream)
            .collect(Collectors.toSet());

        List<AttributeAuthorityConfiguration> authorities = configuration.getAuthorities().stream()
            .filter(conf -> sources.contains(conf.getId())).collect(toList());

        //get attributes from the authorities that were configured as Sources in the ARP
        List<UserAttribute> aggregatedAttributes = getUserAttributes(arpAggregationRequest.getUserAttributes(), authorities);

        LOG.debug("All aggregating attributes based on ARP input {} with result {}", arpAggregationRequest, aggregatedAttributes);

        //Now filter all the attributes based on the values and source of the ARP
        List<UserAttribute> filteredUserAttributes = aggregatedAttributes.stream().map(userAttribute -> {
            List<ArpValue> arpValues = arpAggregationRequest.getArpAttributes().getOrDefault(userAttribute.getName(), emptyList());
            List<String> allowedValues = arpValues.stream()
                .filter(arpValue -> userAttribute.getSource().equals(arpValue.getSource()))
                .map(ArpValue::getValue)
                .collect(toList());
            List<String> filteredValues = userAttribute.getValues().stream()
                .filter(value -> this.valueAllowed(value, allowedValues)).collect(toList());
            return filteredValues.isEmpty() ? Optional.<UserAttribute>empty() :
                Optional.of(new UserAttribute(userAttribute.getName(), filteredValues, userAttribute.getSource()));
        }).filter(Optional::isPresent)
            .map(Optional::get)
            .collect(toList());

        LOG.debug("Finished aggregating and filtering attributes based on ARP in {} millis for input {} with result {}",
            System.currentTimeMillis() - start, arpAggregationRequest, filteredUserAttributes);

        return filteredUserAttributes;
    }

    private boolean valueAllowed(String value, List<String> arpValues) {
        return arpValues.stream().anyMatch(arpValue -> arpValue.equals("*") || arpValue.equalsIgnoreCase(value) ||
            (arpValue.endsWith("*") && arpValue.startsWith(arpValue.substring(0, arpValue.length() - 1))));
    }

    private List<UserAttribute> getUserAttributes(List<UserAttribute> input, Collection<AttributeAuthorityConfiguration> authorityConfigurations) {
        //all of the names of input UserAttributes that at least have one non-empty value
        List<String> inputNames = input.stream().filter(userAttribute -> userAttribute.getValues().stream()
            .anyMatch(StringUtils::hasText))
            .map(UserAttribute::getName)
            .collect(toList());

        //the actual AttributeAggregators to query filtered on the required input parameters
        List<AttributeAggregator> attributeAggregators = authorityConfigurations.stream()
            .map(attributeAuthority -> aggregators.get(attributeAuthority.getId()))
            .filter(attributeAggregator -> inputNames.containsAll(attributeAggregator.attributeKeysRequired()))
            .collect(toList());

        try {
            return forkJoinPool.submit(() -> attributeAggregators.parallelStream().map(aggregator ->
                doAggregate(input, aggregator)).flatMap(List::stream).collect(toList())).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Unable to schedule querying of attribute aggregators.", e);
        }
    }

    private List<UserAttribute> doAggregate(List<UserAttribute> input, AttributeAggregator aggregator) {
        try {
            Optional<String> cacheKey = aggregator.cacheKey(input);
            Optional<List<UserAttribute>> userAttributesFromCache = cache.get(cacheKey);
            if (userAttributesFromCache.isPresent()) {
                return userAttributesFromCache.get();
            }
            List<UserAttribute> userAttributes = aggregator.aggregate(input);
            cache.put(cacheKey, userAttributes);
            return userAttributes;
        } catch (IOException | RuntimeException e) {
            LOG.warn("AttributeAggregator {} threw exception: {} ", aggregator, e);
            return Collections.<UserAttribute>emptyList();
        }
    }

    private boolean allowedAttribute(List<Attribute> attributes, UserAttribute userAttribute) {
        return attributes.stream().anyMatch(attribute -> userAttribute.getSource().equals(attribute.getAttributeAuthorityId()) &&
            userAttribute.getName().equals(attribute.getName()));
    }


}
