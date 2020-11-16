package org.ld.redis;

import org.ld.utils.JsonUtil;
import org.ld.utils.StringUtil;
import org.springframework.data.redis.connection.RedisZSetCommands;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class RedisService {
    private final StringRedisTemplate stringTemplate;
    private final NumberRedisTemplate numberTemplate;

    public RedisService(StringRedisTemplate stringTemplate, NumberRedisTemplate numberTemplate) {
        this.stringTemplate = stringTemplate;
        this.numberTemplate = numberTemplate;
    }

    /**
     * 发送Redis通知
     *
     * @param channel topic
     * @param message 内容
     */
    public void sendMessage(String channel, Object message) {
        stringTemplate.convertAndSend(channel, message);
    }

    /**
     * 存入String
     */
    public void putString(String key, String value) {
        checkStringKey(key);
        stringTemplate.boundValueOps(key).set(value);
    }

    /**
     * 存入 String
     *
     * @param timeout 超时时间,单位 毫秒
     */
    public void putString(String key, String value, long timeout) {
        checkStringKey(key);
        stringTemplate.boundValueOps(key).set(value, timeout, TimeUnit.MILLISECONDS);
    }


    /**
     * 存入 String
     *
     * @param timeout 超时时间
     * @param unit    时间单位
     */
    public void putString(String key, String value, long timeout, TimeUnit unit) {
        checkStringKey(key);
        stringTemplate.boundValueOps(key).set(value, timeout, unit);
    }

    /**
     * redis中获取key对应的值
     */
    public String getString(String key) {
        checkStringKey(key);
        return stringTemplate.boundValueOps(key).get();
    }

    /**
     * 存入Long
     */
    public void putLong(String key, long value) {
        checkStringKey(key);
        numberTemplate.boundValueOps(key).set(value);
    }

    /**
     * 存入 Long
     *
     * @param timeout 超时时间,单位 毫秒
     */
    public void putLong(String key, long value, long timeout) {
        checkStringKey(key);
        numberTemplate.boundValueOps(key).set(value, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 存入 Long
     *
     * @param timeout 超时时间
     * @param unit    时间单位
     */
    public void putLong(String key, long value, long timeout, TimeUnit unit) {
        checkStringKey(key);
        numberTemplate.boundValueOps(key).set(value, timeout, unit);
    }

    /**
     * redis中获取key对应的值
     */
    public Long getLong(String key) {
        checkStringKey(key);
        Number result = numberTemplate.boundValueOps(key).get();
        if (result != null) {
            return result.longValue();
        }
        return null;
    }

    /**
     * redis中获取key对应的值
     */
    public long getLong(String key, long defaultValue) {
        Long value = getLong(key);
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

    /**
     * @see #getInt(String)
     */
    @Deprecated
    public Integer getInteger(String key) {
        return getInt(key);
    }

    /**
     * redis中获取key对应的值
     */
    public Integer getInt(String key) {
        Long value = getLong(key);
        if (value == null) {
            return null;
        }
        return value.intValue();
    }

    /**
     * redis中获取key对应的值
     */
    public int getInt(String key, int defaultValue) {
        Long value = getLong(key);
        if (value == null) {
            return defaultValue;
        }
        return value.intValue();
    }

    /**
     * 存入String
     */
    public <T> void putObject(String key, T value) {
        checkStringKey(key);
        stringTemplate.boundValueOps(key).set(JsonUtil.obj2Json(value));
    }

    /**
     * 存入 Long
     *
     * @param timeout 超时时间
     */
    public <T> void putObject(String key, T value, long timeout) {
        checkStringKey(key);
        stringTemplate.boundValueOps(key).set(JsonUtil.obj2Json(value), timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 存入 Long
     *
     * @param timeout 超时时间
     * @param unit    时间单位
     */
    public <T> void putObject(String key, T value, long timeout, TimeUnit unit) {
        checkStringKey(key);
        stringTemplate.boundValueOps(key).set(JsonUtil.obj2Json(value), timeout, unit);
    }

    /**
     * 删除key
     */
    public void delete(String key) {
        checkStringKey(key);
        stringTemplate.delete(key);
    }

    /**
     * 存入redis列表
     */
    public void rightPushToList(String key, String... values) {
        checkStringKey(key);
        if (values == null) {
            return;
        }
        stringTemplate.boundListOps(key).rightPushAll(values);
    }

    /**
     * 存入redis列表
     */
    public void rightPushToList(String key, Collection<String> values) {
        checkStringKey(key);
        if (values == null) {
            return;
        }
        stringTemplate.boundListOps(key).rightPushAll(values.toArray(new String[0]));
    }

    /**
     * 存入redis列表
     *
     * @param timeout 单位毫秒
     */
    public void rightPushToList(String key, long timeout, String... values) {
        checkStringKey(key);
        if (values == null) {
            return;
        }
        stringTemplate.boundListOps(key).rightPushAll(values);
        stringTemplate.boundListOps(key).expire(timeout, TimeUnit.MILLISECONDS);
    }

    public void rightPushToList(String key, long timeout, TimeUnit unit, String... values) {
        checkStringKey(key);
        if (values == null) {
            return;
        }
        stringTemplate.boundListOps(key).rightPushAll(values);
        stringTemplate.boundListOps(key).expire(timeout, unit);
    }

    /**
     * 存入redis列表
     */
    public void leftPushToList(String key, String... values) {
        checkStringKey(key);
        if (values == null) {
            return;
        }
        stringTemplate.boundListOps(key).leftPushAll(values);
    }


    /**
     * 存入redis列表
     */
    public void leftPushToList(String key, Collection<String> values) {
        checkStringKey(key);
        if (values == null) {
            return;
        }
        stringTemplate.boundListOps(key).leftPushAll(values.toArray(new String[0]));
    }

    /**
     * 存入redis列表
     *
     * @param timeout 单位毫秒
     */
    public void leftPushToList(String key, long timeout, String... values) {
        checkStringKey(key);
        if (values == null) {
            return;
        }
        stringTemplate.boundListOps(key).leftPushAll(values);
        stringTemplate.boundListOps(key).expire(timeout, TimeUnit.MILLISECONDS);
    }

    public void leftPushToList(String key, long timeout, TimeUnit unit, String... values) {
        checkStringKey(key);
        if (values == null) {
            return;
        }
        stringTemplate.boundListOps(key).leftPushAll(values);
        stringTemplate.boundListOps(key).expire(timeout, unit);
    }

    /**
     * 存入redis Set
     */
    public void putToSet(String key, String... values) {
        checkStringKey(key);
        if (values == null) {
            return;
        }
        stringTemplate.boundSetOps(key).add(values);
    }

    /**
     * 存入redis set
     */
    public void putToSet(String key, Collection<String> values) {
        checkStringKey(key);
        if (values == null) {
            return;
        }
        stringTemplate.boundSetOps(key).add(values.toArray(new String[0]));
    }

    /**
     * 存入redis set
     *
     * @param timeout 单位毫秒
     */
    public void putToSet(String key, long timeout, String... values) {
        checkStringKey(key);
        if (values == null) {
            return;
        }
        stringTemplate.boundSetOps(key).add(values);
        stringTemplate.boundSetOps(key).expire(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 存入redis set
     *
     * @param timeout 单位毫秒
     */
    public void putToSet(String key, long timeout, Collection<String> values) {
        checkStringKey(key);
        if (values == null) {
            return;
        }
        stringTemplate.boundSetOps(key).add(values.toArray(new String[0]));
        stringTemplate.boundSetOps(key).expire(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 存入redis ZSet
     */
    public void putToZSet(String key, String value, double score) {
        checkStringKey(key);
        stringTemplate.boundZSetOps(key).add(value, score);
    }

    /**
     * 存入redis ZSet
     */
    public void putToZSet(String key, Set<ZSetOperations.TypedTuple<String>> tuples) {
        checkStringKey(key);
        stringTemplate.boundZSetOps(key).add(tuples);
    }


    /**
     * 存入redis ZSet
     *
     * @param timeout 单位毫秒
     */
    public void putToZSet(String key, long timeout, Set<ZSetOperations.TypedTuple<String>> tuples) {
        checkStringKey(key);
        stringTemplate.boundZSetOps(key).add(tuples);
        stringTemplate.boundZSetOps(key).expire(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 存入redis ZSet
     *
     * @param timeout 单位毫秒
     */
    public void putToZSet(String key, long timeout, String value, double score) {
        checkStringKey(key);
        stringTemplate.boundZSetOps(key).add(value, score);
        stringTemplate.boundZSetOps(key).expire(timeout, TimeUnit.MILLISECONDS);
    }


    /**
     * Set中获取最后插入的值
     */
    public String popStringFromSet(String key) {
        checkStringKey(key);
        return stringTemplate.boundSetOps(key).pop();
    }

    /**
     * ZSet中获取最后插入的值
     */
    public Set<String> rangeByLex(String key, RedisZSetCommands.Range range, RedisZSetCommands.Limit limit) {
        checkStringKey(key);
        return stringTemplate.boundZSetOps(key).rangeByLex(range, limit);
    }

    /**
     * 获取Set所有值
     */
    public Set<String> getMembersFromSet(String key) {
        checkStringKey(key);
        return stringTemplate.boundSetOps(key).members();
    }

    /**
     * 列表中获取最后插入的值
     */
    public String leftPopStringFromList(String key) {
        checkStringKey(key);
        return stringTemplate.boundListOps(key).leftPop();
    }

    /**
     * 列表中获取最早插入的值
     */
    public String rightPopStringFromList(String key) {
        checkStringKey(key);
        return stringTemplate.boundListOps(key).rightPop();
    }

    /**
     * List size
     */
    public long getListSize(String key) {
        checkStringKey(key);
        Long size = stringTemplate.boundListOps(key).size();
        return size == null ? 0 : size;
    }

    /**
     * Set size
     */
    public long getSetSize(String key) {
        checkStringKey(key);
        Long size = stringTemplate.boundSetOps(key).size();
        return size == null ? 0 : size;
    }

    /**
     * 获取列表所有值
     *
     * @param key
     * @return
     */
    public List<String> getListValues(String key) {
        checkStringKey(key);
        return stringTemplate.boundListOps(key).range(0, -1);
    }

    /**
     * 获取列表值
     */
    public List<String> getListValues(String key, long start, long end) {
        checkStringKey(key);
        return stringTemplate.boundListOps(key).range(start, end);
    }


    /**
     * 值放入redis Hash类型
     */
    public void putValueToMap(String nameSpace, String key, String value) {
        checkStringKey(nameSpace);
        checkKey(key);
        BoundHashOperations<String, String, String> boundHashOperations = stringTemplate.boundHashOps(nameSpace);
        boundHashOperations.put(key, value);
    }

    /**
     * 值放入redis Hash类型
     */
    public void putValueToMap(String nameSpace, String key, Number value) {
        checkStringKey(nameSpace);
        checkKey(key);
        BoundHashOperations<String, String, Number> boundHashOperations = numberTemplate.boundHashOps(nameSpace);
        boundHashOperations.put(key, value);
    }

    /**
     * 值放入redis Hash类型
     *
     * @param timeout 单位毫秒
     */
    public void putValueToMap(String nameSpace, String key, String value, long timeout) {
        checkStringKey(nameSpace);
        checkKey(key);
        BoundHashOperations<String, String, String> boundHashOperations = stringTemplate.boundHashOps(nameSpace);
        boundHashOperations.put(key, value);
        boundHashOperations.expire(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 值放入redis Hash类型
     *
     * @param timeout 单位毫秒
     */
    public void putValueToMap(String nameSpace, String key, Number value, long timeout) {
        checkStringKey(nameSpace);
        checkKey(key);
        BoundHashOperations<String, String, Number> boundHashOperations = numberTemplate.boundHashOps(nameSpace);
        boundHashOperations.put(key, value);
        boundHashOperations.expire(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 值放入redis Hash类型
     */
    public void putValueToMap(String nameSpace, String key, String value, long timeout, TimeUnit unit) {
        checkStringKey(nameSpace);
        checkKey(key);
        BoundHashOperations<String, String, String> boundHashOperations = stringTemplate.boundHashOps(nameSpace);
        boundHashOperations.put(key, value);
        boundHashOperations.expire(timeout, unit);
    }

    /**
     * 值放入redis Hash类型
     */
    public void putValueToMap(String nameSpace, String key, Number value, long timeout, TimeUnit unit) {
        checkStringKey(nameSpace);
        checkKey(key);
        BoundHashOperations<String, String, Number> boundHashOperations = numberTemplate.boundHashOps(nameSpace);
        boundHashOperations.put(key, value);
        boundHashOperations.expire(timeout, unit);
    }

    /**
     * Hash类型的数据中取出值
     */
    public String getValueFromMap(String nameSpace, String key) {
        checkStringKey(nameSpace);
        checkKey(key);
        BoundHashOperations<String, String, String> boundHashOperations = stringTemplate.boundHashOps(nameSpace);
        return boundHashOperations.get(key);
    }

    /**
     * Hash类型的数据中取出值
     */
    public Number getNumberValueFromMap(String nameSpace, String key) {
        checkStringKey(nameSpace);
        checkKey(key);
        BoundHashOperations<String, String, Number> boundHashOperations = numberTemplate.boundHashOps(nameSpace);
        return boundHashOperations.get(key);
    }

    /**
     * Map Size
     */
    public long getMapSize(String nameSpace) {
        checkStringKey(nameSpace);
        Long size = stringTemplate.boundHashOps(nameSpace).size();
        return size == null ? 0 : size;
    }

    /**
     * 判断key 是否存在
     *
     * @param key Key
     */
    public boolean hasKey(String key) {
        Boolean result = stringTemplate.hasKey(key);
        return result != null && result;
    }

    /**
     * @see #getOrderUniqueValue()
     */
    @Deprecated
    public long getIncrementValue() {
        return getOrderUniqueValue();
    }

    /**
     * 获取自增长当前值,全局唯一(redis服务器级别)
     */
    public long getOrderUniqueValue() {
        return numberTemplate.getOrderUniqueValue();
    }

    /**
     * 获取自增长当前值,全局唯一(redis服务器级别)
     */
    public long getOrderUniqueValue(long delta) {
        return numberTemplate.getOrderUniqueValue(delta);
    }

    /**
     * 获取自增长当前值,全局唯一(redis服务器级别)
     */
    public long getOrderUniqueValue(String key, long delta) {
        return numberTemplate.getOrderUniqueValue(key, delta);
    }

    /**
     * @see #getOrderUniqueValue()
     */
    @Deprecated
    public long getIncrementValue(long delta) {
        return getOrderUniqueValue(delta);
    }

    /**
     * @see #getOrderUniqueValue()
     */
    @Deprecated
    public long getIncrementValue(String key, long delta) {
        return numberTemplate.getNextValue(key, delta, 1, 0);
    }

    /**
     * 获取随机唯一Key
     */
    public String getRandomUniqueKey() {
        return numberTemplate.getRandomUniqueKey();
    }


    /**
     * 获取redis服务器当前时间(毫秒), 若redis服务故障抛出异常
     */
    public long getCurrentTime() {
        return numberTemplate.getUnsafeCurrentTime();
    }

    /**
     * @see #getCurrentTime()
     */
    @Deprecated
    public long getCurrentUnsafeTime() {
        return numberTemplate.getUnsafeCurrentTime();
    }

    /**
     * 获取redis服务器当前时间(毫秒), 若redis服务故障,返回本地时间,业务对时间无要求时可使用
     */
    public Long getCurrentSafeTime() {
        return numberTemplate.getCurrentTime();
    }

    /**
     * 检查有效性
     */
    private void checkKey(Object key) {
        if (key instanceof String) {
            checkStringKey((String) key);
        }
        if (key == null) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * 检查有效性
     */
    private void checkStringKey(String key) {
        if (StringUtil.isEmpty(key)) {
            throw new IllegalArgumentException();
        }
    }

}
