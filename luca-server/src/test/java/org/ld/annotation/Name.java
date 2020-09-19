package org.ld.annotation;


import java.lang.annotation.*;

@Target(ElementType.FIELD) // 可以注解成员变量
@Retention(RetentionPolicy.RUNTIME) //   1.SOURCE:在源文件中有效（即源文件保留）2.CLASS:在class文件中有效（即class保留）3.RUNTIME:在运行时有效（即运行时保留）
@Documented // 用于描述其它类型的annotation应该被作为被标注的程序成员的公共API
public @interface Name {
    String value() default "";
}