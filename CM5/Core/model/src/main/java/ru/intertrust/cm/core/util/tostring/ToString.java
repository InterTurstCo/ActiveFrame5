package ru.intertrust.cm.core.util.tostring;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;

public abstract class ToString {

    private static final ConcurrentMap<Class<?>, List<Field>> fieldsByClass = new ConcurrentHashMap<>();

    private static final ThreadLocal<ThreadContext> threadContexts = new ThreadLocal<ThreadContext>() {

        @Override
        protected ThreadContext initialValue () {
            return new ThreadContext();
        }

    };

    @Nonnull
    public static String generate (final @Nonnull Object object) {
        return generate(object, null);
    }

    @Nonnull
    public static String generate (final @Nonnull Object object, final Object prefix) {

        if (object == null) {
            throw new IllegalArgumentException("object must be not-null.");
        }

        final ThreadContext threadContext = threadContexts.get();
        final String objectId = threadContext.objectIds.get(object);

        if (objectId != null) {
            return objectId;
        }

        final Class<?> clazz = object.getClass();
        final String sPrefix0 = (prefix == null) ? null : prefix.toString();
        final String sPrefix1 = (sPrefix0 == null || sPrefix0.isEmpty()) ? clazz.getSimpleName() : sPrefix0;
        Integer clazzCounter = threadContext.clazzCounters.get(clazz);
        threadContext.clazzCounters.put(clazz, (clazzCounter == null) ? clazzCounter = 1 : ++clazzCounter);
        final String sPrefix = (clazzCounter == 1) ? sPrefix1 : sPrefix1 + "#" + clazzCounter;
        threadContext.objectIds.put(object, sPrefix);

        if (threadContext.first == null) {
            threadContext.first = object;
        }

        try {

            final StringBuilder sb = (new StringBuilder(sPrefix)).append("{");
            boolean b = false;

            for (final Field field : getElements(clazz)) {

                if (b) {
                    sb.append(", ");
                } else {
                    b = true;
                }

                sb.append(field.getName()).append("=").append(field.get(object));

            }

            return sb.append("}").toString();

        } catch (final IllegalAccessException e) {

            throw new RuntimeException(e);

        } finally {

            if (threadContext.first == object) {
                threadContexts.remove();
            }

        }

    }

    private static List<Field> getElements (final Class<?> clazz) {

        List<Field> fields = fieldsByClass.get(clazz);

        if (fields != null) {

            return fields;

        } else if (clazz == Object.class) {

            fields = Collections.EMPTY_LIST;

        } else {

            fields = new ArrayList<>(getElements(clazz.getSuperclass()));

            for (final Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(ToStringElement.class)) {

                    fields.add(field);

                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }

                }
            }

        }

        final List<Field> fieldsExist = fieldsByClass.putIfAbsent(clazz, fields);
        return (fieldsExist == null) ? fields : fieldsExist;

    }

    private static class ThreadContext {

        Object first;
        final Map<Object, String> objectIds = new HashMap<>();
        final Map<Class<?>, Integer> clazzCounters = new HashMap<>();

    }

    private ToString () {
    }

}