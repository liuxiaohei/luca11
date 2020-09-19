package org.ld.utils;

import org.ld.exception.CodeStackException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;

@SuppressWarnings("unused")
@Configuration
public class SpringBeanFactory implements ApplicationContextAware {

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

    public static <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> clazz) {
        try {
            return applicationContext.findAnnotationOnBean(beanName,clazz);
        } catch (BeansException e) {
            throw new CodeStackException(e);
        }
    }

    public static <T> T getBean(String beanName, Class<T> clazz) {
        try {
            return applicationContext.getBean(beanName,clazz);
        } catch (BeansException e) {
            throw new CodeStackException(e);
        }
    }

    public static String[] getBeanNamesForType(@Nullable Class<?> clazz) {
        try {
            return applicationContext.getBeanNamesForType(clazz);
        } catch (BeansException e) {
            throw new CodeStackException(e);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        applicationContext = context;
    }

    /**
     * 获取Bean 实例
     */
    public static String[] getBeanNamesForAnnotation(Class<? extends Annotation> clazz) {
        try {
            return applicationContext.getBeanNamesForAnnotation(clazz);
        } catch (BeansException e) {
            throw new CodeStackException(e);
        }
    }
}
