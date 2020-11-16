package org.ld.annotation;

import java.lang.annotation.*;

/**
 * Description: 同步处理器
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Synchronized {

    int argIndex() default 0;

    ArgType argType();

    String keyName();

    int expire() default 20 * 1000;

    int timeout() default 60 * 1000;

    boolean timeoutException() default true;

    /**
     * KEY 类型
     */
    enum ArgType {
        //基本类型参数
        ARG_NORMAL,
        //Bean类型参数
        ARG_BEAN,
        //Map类型参数
        ARG_MAP,
        //方法级同步
        ARG_METHOD;
    }
}
