package org.ld.redis;

import lombok.extern.slf4j.Slf4j;
import org.ld.utils.SleepUtil;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public abstract class BaseRedisTemplate<K, V> extends RedisTemplate<K, V> {
    protected static final byte[] INCREMENT_KEY = "increment_key".getBytes();
    protected static final byte[] UNIQUE_KEY = "unique_key".getBytes();
    private static final int RETRY_COUNT = 1;
    private static Long delta; // 时间差

    public BaseRedisTemplate() {
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        setKeySerializer(stringSerializer);
        setValueSerializer(stringSerializer);
        setHashKeySerializer(stringSerializer);
        setHashValueSerializer(stringSerializer);
    }

    /**
     * 获取redis 当前时间
     */
    public Long getCurrentTime() {
        return getCurrentTime(RETRY_COUNT);
    }

    /**
     * 获取当前时间
     */
    private Long getCurrentTime(int retry) {
        if (delta == null) {
            Long time = execute(RedisServerCommands::time);
            if (time == null && retry <= RETRY_COUNT) {
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
        Long time = getCurrentTime();
        if (time == null) {
            time = System.currentTimeMillis();
        }
        return time;
    }

    /**
     * 获取顺序唯一值
     */
    public long getOrderUniqueValue() {
        return getOrderUniqueValue(1);
    }

    /**
     * 获取顺序唯一值
     */
    public long getOrderUniqueValue(long delta) {
        return getNextValue(INCREMENT_KEY, getCurrentTime(), delta, 0);
    }

    /**
     * 获取顺序唯一值
     */
    public long getOrderUniqueValue(String key, long delta) {
        return getNextValue(key, getCurrentTime(), delta, 0);
    }

    /**
     * 获取随机唯一Key
     */
    public String getRandomUniqueKey() {
        return getRandomUniqueKeys(1).get(0);
    }

    /**
     * 获取随机唯一Key
     */
    public List<String> getRandomUniqueKeys(int count) {
        return getNextValues(UNIQUE_KEY, 1000 * 1000L, getRandomInValue(1000) + 1, 0, count)
                .stream().map(value -> to62RadixString(value)).collect(Collectors.toList());
    }

    /**
     * 转换为62进制
     */
    private static String to62RadixString(long i) {
        return toRadixString(i, 62);
    }

    private final static int MIN_RADIX = 2;
    private final static int MAX_RADIX = 62;

    /**
     * All possible chars for representing a number as a String
     */
    private final static char[] DIGITS = {
            '0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b',
            'c', 'd', 'e', 'f', 'g', 'h',
            'i', 'j', 'k', 'l', 'm', 'n',
            'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F',
            'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z',
    };

    /**
     * 进制转换
     */
    private static String toRadixString(long i, int radix) {
        if (radix < MIN_RADIX || radix > MAX_RADIX)
            radix = 10;
        if (radix == 10)
            return Long.toString(i);
        char[] buf = new char[65];
        int charPos = 64;
        boolean negative = (i < 0);

        if (!negative) {
            i = -i;
        }

        while (i <= -radix) {
            buf[charPos--] = DIGITS[(int) (-(i % radix))];
            i = i / radix;
        }
        buf[charPos] = DIGITS[(int) (-i)];

        if (negative) {
            buf[--charPos] = '-';
        }

        return new String(buf, charPos, (65 - charPos));
    }

    private static int getRandomInValue(int value) {
        return (int) (Math.random() * value);
    }

    /**
     * 获取下一个值
     */
    public Long getNextValue(String key, Long initValue, long delta, long timeout) {
        return getNextValues(key.getBytes(), initValue, delta, timeout, 1).get(0);
    }

    /**
     * 获取下一个值
     */
    public Long getNextValue(byte[] key, Long initValue, long delta, long timeout) {
        return getNextValues(key, initValue, delta, timeout, 1).get(0);
    }

    /**
     * 获取下一个值
     *
     * @param key       Key
     * @param initValue 初始值
     * @param delta     差值
     * @param timeout   过期时间
     * @param count     生成总数
     */
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
