package org.ld.examples.annotation;


import java.lang.annotation.*;

/**
 * Created by mingwei on 12/2/16.
 * 性别注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Gender {

    enum GenderType {

        Male("男"),
        Female("女"),
        Other("中性");

        private String genderStr;

        private GenderType(String arg0) {
            this.genderStr = arg0;
        }

        @Override
        public String toString() {
            return genderStr;
        }
    }

    GenderType gender() default GenderType.Male;

}