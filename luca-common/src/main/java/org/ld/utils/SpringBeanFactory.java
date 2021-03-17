package org.ld.utils;

import lombok.SneakyThrows;
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
    @SneakyThrows
    public static <T> T getBean(Class<T> clazz) {
        return applicationContext.getBean(clazz);
    }

    /**
     * 获取Bean 实例 需要自己强转
     */
    @SneakyThrows
    public static Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    /**
     * 获取Bean 类型 需要自己强转
     */
    @SneakyThrows
    public static Class<?> getType(String name) {
        return applicationContext.getType(name);
    }

    @SneakyThrows
    public static <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> clazz) {
        return applicationContext.findAnnotationOnBean(beanName, clazz);
    }

    @SneakyThrows
    public static <T> T getBean(String beanName, Class<T> clazz) {
        return applicationContext.getBean(beanName, clazz);
    }

    @SneakyThrows
    public static String[] getBeanNamesForType(@Nullable Class<?> clazz) {
        return applicationContext.getBeanNamesForType(clazz);
    }

    @SneakyThrows
    @Override
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }

    /**
     * 获取Bean 实例
     */
    @SneakyThrows
    public static String[] getBeanNamesForAnnotation(Class<? extends Annotation> clazz) {
        return applicationContext.getBeanNamesForAnnotation(clazz);
    }
}
