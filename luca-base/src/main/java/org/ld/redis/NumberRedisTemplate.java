package org.ld.redis;

import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 */
public class NumberRedisTemplate extends BaseRedisTemplate<String, Number> {

    private NumberRedisTemplate() {
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        NumberRedisSerializer numberSerializer = new NumberRedisSerializer();
        setKeySerializer(stringSerializer);
        setValueSerializer(numberSerializer);
        setHashKeySerializer(stringSerializer);
        setHashValueSerializer(numberSerializer);
    }

    /**
     * Constructs a new <code>StringLongRedisTemplate</code> instance ready to be used.
     *
     * @param connectionFactory connection factory for creating new connections
     */
    public NumberRedisTemplate(RedisConnectionFactory connectionFactory) {
        this();
        setConnectionFactory(connectionFactory);
        afterPropertiesSet();
    }

    protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
        return new DefaultStringRedisConnection(connection);
    }
}
