package org.ld.redis;

import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Optional;

/**
 * 返回long类型的redis模板
 */
public class LongRedisTemplate extends RedisTemplate<String, Long> {

    public LongRedisTemplate(RedisConnectionFactory connectionFactory) {
        var stringSerializer = new StringRedisSerializer();
        setKeySerializer(stringSerializer);
        setHashKeySerializer(stringSerializer);
        var longSerializer = new RedisSerializer<Long>() {
            public Long deserialize(byte[] bytes) {
                return Optional.ofNullable(bytes).map(String::new).map(Long::valueOf).orElse(null);
            }
            public byte[] serialize(Long t) {
                return t == null ? new byte[0] : t.toString().getBytes();
            }
        };
        setValueSerializer(longSerializer);
        setHashValueSerializer(longSerializer);
        setConnectionFactory(connectionFactory);
        afterPropertiesSet();
    }

    @SuppressWarnings("all")
    protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
        return new DefaultStringRedisConnection(connection);
    }

    /**
     * 获取当前redis时间
     */
    public Long getCurrentTime() {
        return execute(RedisServerCommands::time);
    }
}
