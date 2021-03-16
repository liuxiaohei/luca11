package org.ld.aspect;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.ld.annotation.Synchronized;
import org.ld.exception.CodeStackException;
import org.ld.redis.NumberRedisTemplate;
import org.ld.utils.JsonUtil;
import org.ld.utils.SleepUtil;
import org.ld.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 基于Redis实现的分布式锁
 */
@Slf4j
@Component
@Aspect
@Order(0)
public class SynchronizationAspect {

    @Autowired
    private NumberRedisTemplate numberRedisTemplate;

    @Around("@annotation(sync)")
    public Object cutPoint(ProceedingJoinPoint point, Synchronized sync) throws Throwable {

            return executePoint(point, sync);

    }

    private Object executePoint(ProceedingJoinPoint point, Synchronized sync) throws Throwable {
        MethodSignature methodSignature = getSignature(point);
        Method method = methodSignature.getMethod();
        String methodName = point.getTarget().getClass() + "#" + method.getName();
        Synchronized.ArgType argType = sync.argType();
        String key = null;
        Object[] argsArray = point.getArgs();
        if (argsArray == null || argsArray.length == 0) {
            key = methodName;
        } else {
            Object argObj = argsArray[sync.argIndex()];
            switch (argType) {
                case ARG_NORMAL://处理基本参数
                    key = argObj.toString();
                    break;
                case ARG_BEAN://处理Bean类型
                case ARG_MAP://处理Map类型
                    key = getBeanKey(sync, argObj);
                    break;
                case ARG_METHOD://处理方法类型
                    key = sync.keyName();
                    if (StringUtil.isEmpty(key)) {
                        key = methodName;
                    }
                    break;
            }
        }
        return waitIfNecessary(point, DigestUtils.md5Hex(methodName + key), sync);
    }

    private Object waitIfNecessary(ProceedingJoinPoint point, String key, Synchronized sync) throws Throwable {
        Object result = null;
        Boolean lock = false;
        try {
            if (StringUtil.isEmpty(key)) {
                return point.proceed();
            }
            var boundValueOps = numberRedisTemplate.boundValueOps(key);
            long time = numberRedisTemplate.getUnsafeCurrentTime();
            long lockTime = time + sync.expire();
            long timeoutTime = time + sync.timeout();
            while (!Optional.ofNullable(lock).orElse(false)) {
                // 设置同步标示
                lock = boundValueOps.setIfAbsent(lockTime);
                if (Optional.ofNullable(lock).orElse(false)) { // 加锁成功
                    boundValueOps.set(lockTime, sync.expire() * 3, TimeUnit.MILLISECONDS);
                    result = point.proceed();
                    lock = true;
                } else if (sync.timeoutException()) { //超时需要抛出异常
                    long currentTime = numberRedisTemplate.getUnsafeCurrentTime();
                    if (timeoutTime < currentTime) {
                        throw new CodeStackException("超过超时时间");
                    }
                    SleepUtil.sleep(10);
                } else {
                    long lastLockTime = Optional.ofNullable(boundValueOps.get()).map(Number::longValue).orElse(0L);
                    long currentTime = numberRedisTemplate.getUnsafeCurrentTime();
                    if (lastLockTime < currentTime) {// 锁已过期,执行目标方法
                        boundValueOps.set(currentTime + sync.expire());
                        result = point.proceed();
                        lock = true;
                    } else {//锁未过期,继续等待
                        SleepUtil.sleep(10);
                    }
                }
            }
        } catch (Exception e) {
            throw CodeStackException.of(e);
        } finally {
            if (StringUtil.isNotEmpty(key)) {
                numberRedisTemplate.delete(key);
            }
        }
        return result;
    }

    /**
     * 获取对象的key
     */
    private static String getBeanKey(Synchronized sync, Object argObj) {
        StringBuilder key = new StringBuilder();
        String[] keyParts = sync.keyName().split("|");
        for (int i = 0; i < keyParts.length; i++) {
            key.append(JsonUtil.obj2Json(argObj));
        }
        return key.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Signature> T getSignature(ProceedingJoinPoint point) {
        return (T) point.getSignature();
    }
}