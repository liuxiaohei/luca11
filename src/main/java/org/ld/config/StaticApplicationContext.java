package org.ld.config;

import org.ld.exception.CodeStackException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

@SuppressWarnings("unused")
@Configuration
public class StaticApplicationContext implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * 获取Bean 实例
     */
    public static <T> T getBean(Class<T> clazz) {
        try {
            return applicationContext.getBean(clazz);
        } catch (BeansException e) {
            throw new CodeStackException(e);
        }
    }

    /**
     * 获取Bean 实例 需要自己强转
     */
    public static Object getBean(String name) {
        try {
            return applicationContext.getBean(name);
        } catch (BeansException e) {
            throw new CodeStackException(e);
        }
    }

    /**
     * 获取Bean 类型 需要自己强转
     */
    public static Class<?> getType(String name) {
        try {
            return applicationContext.getType(name);
        } catch (BeansException e) {
            throw new CodeStackException(e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }
}
