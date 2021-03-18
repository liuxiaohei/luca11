package org.ld.redis;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

/**
 */
public class BooleanRedisSerializer implements RedisSerializer<Boolean> {

    /**
     * Creates a new {@link BooleanRedisSerializer}
     */
    public BooleanRedisSerializer() {
    }


    /**
     * 解码
     */
    @Override
    public Boolean deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return Boolean.parseBoolean(new String(bytes));
    }

    /**
     * 编码
     */
    @Override
    public byte[] serialize(Boolean t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }

        return t.toString().getBytes();
    }

}
