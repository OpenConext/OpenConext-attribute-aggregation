package aa.cache;

import aa.model.UserAttribute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambdaworks.redis.RedisClient;
import com.lambdaworks.redis.SetArgs;
import com.lambdaworks.redis.api.StatefulRedisConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class RedisUserAttributeCache implements UserAttributeCache {

  private final static Logger LOG = LoggerFactory.getLogger(RedisUserAttributeCache.class);

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final StatefulRedisConnection<String, String> connection;
  private TypeReference<List<UserAttribute>> typeRef = new TypeReference<List<UserAttribute>>() {
  };
  private final long cacheDuration;

  public RedisUserAttributeCache(String redisUrl, long cacheDuration) {
    RedisClient redisClient = RedisClient.create(redisUrl);
    this.connection = redisClient.connect();
    this.cacheDuration = cacheDuration;
  }

  @Override
  public Optional<List<UserAttribute>> get(Optional<String> cacheKey) {
    if (!cacheKey.isPresent()) {
      return Optional.empty();
    }
    String json = this.connection.sync().get(cacheKey.get());
    if (StringUtils.hasText(json)) {
      try {
        List<UserAttribute> userAttributes = objectMapper.readValue(json, typeRef);
        LOG.debug("Returning userAttributes from cache {}", userAttributes);
        return Optional.of(userAttributes);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    return Optional.empty();
  }

  @Override
  public void put(Optional<String> cacheKey, List<UserAttribute> userAttributes) {
    if (cacheKey.isPresent() && !CollectionUtils.isEmpty(userAttributes)) {
      LOG.debug("Putting userAttributes in cache {} with key {}", userAttributes, cacheKey.get());
      try {
        this.connection.sync().set(
            cacheKey.get(),
            objectMapper.writeValueAsString(userAttributes),
            SetArgs.Builder.px(cacheDuration));
      } catch (JsonProcessingException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
