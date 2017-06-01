package aa.cache;

import aa.model.UserAttribute;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.List;

public class RedisUserAttributeCache extends AbstractUserAttributeCache {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JedisPool pool;
    private TypeReference<List<UserAttribute>> typeRef = new TypeReference<List<UserAttribute>>() {
    };

    public RedisUserAttributeCache(String redisUrl, long cacheDuration) {
        super(cacheDuration);
        pool = new JedisPool(new JedisPoolConfig(), redisUrl);
    }

    @Override
    protected List<UserAttribute> doGet(String cacheKey) throws IOException {
        try (Jedis jedis = pool.getResource()) {
            String json = jedis.get(cacheKey);
            return StringUtils.hasText(json) ? objectMapper.readValue(json, typeRef) : null;
        }
    }

    @Override
    protected void doPut(String cacheKey, List<UserAttribute> userAttributes) throws JsonProcessingException {
        try (Jedis jedis = pool.getResource()) {
            jedis.psetex(cacheKey, getCacheDuration(), objectMapper.writeValueAsString(userAttributes));
        }
    }
}
