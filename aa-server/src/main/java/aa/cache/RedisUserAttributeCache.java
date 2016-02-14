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

public class RedisUserAttributeCache extends AbstractUserAttributeCache {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final StatefulRedisConnection<String, String> connection;
  private TypeReference<List<UserAttribute>> typeRef = new TypeReference<List<UserAttribute>>() {
  };

  public RedisUserAttributeCache(String redisUrl, long cacheDuration) {
    super(cacheDuration);
    RedisClient redisClient = RedisClient.create(redisUrl);
    this.connection = redisClient.connect();
  }

  @Override
  protected List<UserAttribute> doGet(String cacheKey) throws IOException {
    String json = this.connection.sync().get(cacheKey);
    if (StringUtils.hasText(json)) {
      return objectMapper.readValue(json, typeRef);
    }
    return null;
  }

  @Override
  protected void doPut(String cacheKey, List<UserAttribute> userAttributes) throws JsonProcessingException {
    this.connection.sync().set(
        cacheKey,
        objectMapper.writeValueAsString(userAttributes),
        SetArgs.Builder.px(getCacheDuration()));
  }
}
