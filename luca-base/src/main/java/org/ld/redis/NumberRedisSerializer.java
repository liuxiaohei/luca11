package org.ld.redis;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 */
public class NumberRedisSerializer implements RedisSerializer<Number> {

    /**
     * Creates a new {@link NumberRedisSerializer}
     */
    public NumberRedisSerializer() {
    }


    /**
     * 解码
     */
    public Number deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            Double value = Double.valueOf(new String(bytes));
            long longValue = value.longValue();
            if (value == longValue) {
                if (between(longValue, Integer.MIN_VALUE, Integer.MAX_VALUE)) {
                    return value.intValue();
                } else {
                    return longValue;
                }
            } else {
                return value;
            }
        } catch (Exception e) {
            return null;
        }
    }

    private boolean between(long value, long min, long max) {
        return value >= min && value <= max;
    }

    private boolean between(double value, double min, double max) {
        return value >= min && value <= max;
    }

    /**
     * 编码
     */
    public byte[] serialize(Number t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }

        return t.toString().getBytes();
    }

}
