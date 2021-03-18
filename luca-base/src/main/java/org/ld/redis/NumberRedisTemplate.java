package org.ld.redis;

import lombok.SneakyThrows;
import org.ld.utils.SleepUtil;
import org.ld.utils.StringUtil;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class NumberRedisTemplate extends RedisTemplate<String, Number> {

    public NumberRedisTemplate(RedisConnectionFactory connectionFactory) {
        var stringSerializer = new StringRedisSerializer();
        var numberSerializer = new RedisSerializer<Number>() {
            @SuppressWarnings("all")
            @SneakyThrows
            public Number deserialize(byte[] bytes) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                var value = Double.valueOf(new String(bytes));
                var longValue = value.longValue();
                return value == longValue
                        ? (
                        longValue >= (long) Integer.MIN_VALUE && longValue <= (long) Integer.MAX_VALUE
                                ? value.intValue()
                                : longValue)
                        : value;
            }

            @SneakyThrows
            public byte[] serialize(Number t) {
                return t == null ? new byte[0] : t.toString().getBytes();
            }
        };
        setKeySerializer(stringSerializer);
        setValueSerializer(numberSerializer);
        setHashKeySerializer(stringSerializer);
        setHashValueSerializer(numberSerializer);
        setConnectionFactory(connectionFactory);
        afterPropertiesSet();
    }

    @SuppressWarnings("all")
    protected RedisConnection preProcessConnection(RedisConnection connection, boolean existingConnection) {
        return new DefaultStringRedisConnection(connection);
    }

    protected static final byte[] INCREMENT_KEY = "increment_key".getBytes();
    protected static final byte[] UNIQUE_KEY = "unique_key".getBytes();
    private static Long delta; // 时间差

    public Long getCurrentTime() {
        return getCurrentTime(1);
    }

    private Long getCurrentTime(int retry) {
        if (delta == null) {
            Long time = execute(RedisServerCommands::time);
            if (time == null && retry <= 1) {
                SleepUtil.sleep(5);
                return getCurrentTime(++retry);
            } else {
                if (time == null) {
                    delta = null;
                } else {
                    delta = time - System.currentTimeMillis();
                }
                return time;
            }
        } else {
            return System.currentTimeMillis() + delta;
        }
    }

    /**
     * 获取时间(redis 获取不到,取本地时间)
     */
    public long getUnsafeCurrentTime() {
        return Optional.ofNullable(getCurrentTime()).orElseGet(System::currentTimeMillis);
    }

    public long getOrderUniqueValue(long delta) {
        return getNextValue(INCREMENT_KEY, getCurrentTime(), delta, 0);
    }

    public long getOrderUniqueValue(String key, long delta) {
        return getNextValue(key, getCurrentTime(), delta, 0);
    }

    public List<String> getRandomUniqueKeys(int count) {
        return getNextValues(UNIQUE_KEY, 1000 * 1000L, ((int) (Math.random() * 1000)) + 1, 0, count)
                .stream().map(i -> StringUtil.toRadixString(i, 62)).collect(Collectors.toList());
    }

    public Long getNextValue(String key, Long initValue, long delta, long timeout) {
        return getNextValues(key.getBytes(), initValue, delta, timeout, 1).get(0);
    }

    public Long getNextValue(byte[] key, Long initValue, long delta, long timeout) {
        return getNextValues(key, initValue, delta, timeout, 1).get(0);
    }

    public List<Long> getNextValues(final byte[] key, Long initValue, long delta, long timeout, int count) {
        List<Object> commandResult = executePipelined((RedisCallback<Long>) connection -> {
            if (initValue != null) {
                connection.setNX(key, initValue.toString().getBytes());
            }
            int c = count;
            while (c-- > 0) {
                connection.incrBy(key, delta);
            }
            if (timeout > 0) {
                connection.expire(key, timeout);
            }
            return null;
        });
        return commandResult.stream()
                .filter(e -> e instanceof Long)
                .map(e -> (Long) e)
                .collect(Collectors.toList());
    }
}
