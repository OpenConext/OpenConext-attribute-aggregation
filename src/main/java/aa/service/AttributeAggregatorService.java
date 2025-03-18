package aa.service;

import aa.aggregators.AttributeAggregator;
import aa.cache.UserAttributeCache;
import aa.config.AuthorityConfiguration;
import aa.model.ArpAggregationRequest;
import aa.model.ArpValue;
import aa.model.AttributeAuthorityConfiguration;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class AttributeAggregatorService {

    private final static Logger LOG = LoggerFactory.getLogger(AttributeAggregatorService.class);
    private static final Logger ANALYTICS_LOG = LoggerFactory.getLogger("analytics");

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

    public List<UserAttribute> aggregateBasedOnArp(ArpAggregationRequest arpAggregationRequest) {
        long start = System.currentTimeMillis();
        ANALYTICS_LOG.info("Started to aggregate attributes based on ARP for input {}", arpAggregationRequest);

        Set<String> sources = arpAggregationRequest.getArpAttributes().values().stream()
            .flatMap(arpValues -> arpValues.stream().map(ArpValue::getSource))
            .collect(Collectors.toSet());

        List<AttributeAuthorityConfiguration> authorities = configuration.getAuthorities().stream()
            .filter(conf -> sources.contains(conf.getId())).collect(toList());

        //get attributes from the authorities that were configured as Sources in the ARP
        List<UserAttribute> aggregatedAttributes = getUserAttributes(
                arpAggregationRequest.getUserAttributes(),
                arpAggregationRequest.getArpAttributes(),
                authorities);

        ANALYTICS_LOG.info("All aggregating attributes based on ARP input {} with result {}", arpAggregationRequest, aggregatedAttributes);

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

        ANALYTICS_LOG.info("Finished aggregating and filtering attributes based on ARP in {} millis for input {} with result {}",
            System.currentTimeMillis() - start, arpAggregationRequest, filteredUserAttributes);

        return filteredUserAttributes;
    }

    private boolean valueAllowed(String value, List<String> arpValues) {
        return arpValues.stream().anyMatch(arpValue -> arpValue.equals("*") || arpValue.equalsIgnoreCase(value) ||
            (arpValue.endsWith("*") && value.startsWith(arpValue.substring(0, arpValue.length() - 1))));
    }

    private List<UserAttribute> getUserAttributes(List<UserAttribute> input,
                                                  Map<String, List<ArpValue>> arpAttributes,
                                                  Collection<AttributeAuthorityConfiguration> authorityConfigurations) {
        //all the names of input UserAttributes that at least have one non-empty value
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
                doAggregate(input, aggregator, arpAttributes)).flatMap(List::stream).collect(toList())).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Unable to schedule querying of attribute aggregators.", e);
        }
    }

    private List<UserAttribute> doAggregate(List<UserAttribute> input,
                                            AttributeAggregator aggregator,
                                            Map<String, List<ArpValue>> arpAttributes) {
        try {
            Optional<String> cacheKey = aggregator.cacheKey(input);
            Optional<List<UserAttribute>> userAttributesFromCache = cache.get(cacheKey);
            if (userAttributesFromCache.isPresent()) {
                return userAttributesFromCache.get();
            }
            List<UserAttribute> userAttributes = aggregator.aggregate(input, arpAttributes);
            List<UserAttribute> filteredAttributes = aggregator.filterInvalidResponses(userAttributes);
            cache.put(cacheKey, filteredAttributes);
            return filteredAttributes;
        } catch (IOException | RuntimeException e) {
            LOG.warn("AttributeAggregator {} threw exception: {} ", aggregator.getAttributeAuthorityId(), e);
            return Collections.emptyList();
        }
    }

}
