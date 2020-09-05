package org.ld.examples.annotation;


import java.lang.annotation.*;

/**
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Profile {

    int id() default -1;

    int height() default 0;

    String nativePlace() default "";
}