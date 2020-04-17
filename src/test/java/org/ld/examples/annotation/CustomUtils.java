package org.ld.examples.annotation;

import org.ld.utils.Logger;

import java.lang.reflect.Field;

public class CustomUtils {

    private static Logger Log = Logger.newInstance();

    public static void getInfo(Class<?> clazz) {
        Field fields[] = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Name.class)) {
                Name arg0 = field.getAnnotation(Name.class);
                String name = arg0.value();
                Log.info(() -> "Gmw" + "name=" + name);
            }
            if (field.isAnnotationPresent(Gender.class)) {
                Gender arg0 = field.getAnnotation(Gender.class);
                String gender = arg0.gender().toString();
                Log.info(() -> "Gmw" + "gender=" + gender);
            }
            if (field.isAnnotationPresent(Profile.class)) {
                Profile arg0 = field.getAnnotation(Profile.class);
                String profile = "[id=" + arg0.id() + ",height=" + arg0.height() + ",nativePlace=" + arg0.nativePlace() + "]";
                Log.info(() -> "Gmw" + "profile=" + profile);
            }
        }
    }
}
