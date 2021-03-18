package org.ld.redis;

import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 */
public class BooleanRedisTemplate extends BaseRedisTemplate<String, Boolean> {

    private BooleanRedisTemplate() {
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        BooleanRedisSerializer booleanSerializer = new BooleanRedisSerializer();
        setKeySerializer(stringSerializer);
        setValueSerializer(booleanSerializer);
        setHashKeySerializer(stringSerializer);
        setHashValueSerializer(booleanSerializer);
    }

    /**
     * Constructs a new <code>StringLongRedisTemplate</code> instance ready to be used.
     *
     * @param connectionFactory connection factory for creating new connections
     */
    public BooleanRedisTemplate(RedisConnectionFactory connectionFactory) {
        this();
        setConnectionFactory(connectionFactory);
        afterPropertiesSet();
    }

    @Override
    protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
        return new DefaultStringRedisConnection(connection);
    }
}
