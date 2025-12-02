package bl0.bjs.framework.utils;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class ClassUtils {
    public static boolean isSubclassOf(Class<?> clazz, Class<?> parentClass) {
        while (clazz != null) {
            if (clazz.equals(parentClass)) {
                return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    public static Class<?> getSuperclass(Class<?> clazz, Class<?> wantedClass) {
        while (clazz != null) {
            if (clazz.equals(wantedClass)) {
                return clazz;
            }
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static Class<?> getGenericParameter(Class<?> clazz, Class<?> baseClass) {
        while (clazz != null && clazz != Object.class) {
            Type superclass = clazz.getGenericSuperclass();

            if (superclass instanceof ParameterizedType) {
                ParameterizedType parameterized = (ParameterizedType) superclass;
                Type raw = parameterized.getRawType();

                if (raw instanceof Class && baseClass.isAssignableFrom((Class<?>) raw)) {
                    Type actualType = parameterized.getActualTypeArguments()[0];

                    if (actualType instanceof Class) {
                        return (Class<?>) actualType;
                    } else if (actualType instanceof ParameterizedType) {
                        return (Class<?>) ((ParameterizedType) actualType).getRawType();
                    }
                }
            }

            clazz = clazz.getSuperclass();
        }

        return null;
    }
}
