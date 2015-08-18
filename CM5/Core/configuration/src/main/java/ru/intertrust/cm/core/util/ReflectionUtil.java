package ru.intertrust.cm.core.util;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 17.08.2015
 *         Time: 1:00
 */
public class ReflectionUtil {

    public static void fillAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            fillAllFields(fields, type.getSuperclass());
        }

    }
}
