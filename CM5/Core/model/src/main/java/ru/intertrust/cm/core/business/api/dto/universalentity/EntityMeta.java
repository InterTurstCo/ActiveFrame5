package ru.intertrust.cm.core.business.api.dto.universalentity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;

public abstract class EntityMeta {

    private static final ConcurrentMap<Class<?>, Object> metas = new ConcurrentHashMap<>();

    @Nonnull
    public static <T extends Entity> T createMeta (final @Nonnull Class<T> clazz) {

        if (!clazz.isInterface()) {
            throw new IllegalArgumentException("clazz must be an interface");
        }

        Object result = metas.get(clazz);

        if (result == null) {

            final TypeName ann = clazz.getAnnotation(TypeName.class);

            if (ann == null) {
                throw new ConfigurationException("'" + clazz + "' must be annotated by '" + TypeName.class + "'");
            }

            final Object exist = metas.putIfAbsent(clazz, result = createProxy(clazz, ann.value(), false, true));

            if (exist != null) {
                result = exist;
            }

        }

        return clazz.cast(result);

    }

    @Nonnull
    private static Object createProxy (final @Nonnull Class<?> clazz, final @Nonnull String name, final boolean isAddPrefix, final boolean isAllowToString) {
        return Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new MetaInvocationHandler(clazz, name, isAddPrefix, isAllowToString));
    }

    private static class MetaInvocationHandler implements InvocationHandler {

        private final Class<?> clazz;
        private final String name;
        private final boolean isAddPrefix;
        private final boolean isAllowToString;

        MetaInvocationHandler (final @Nonnull Class<?> clazz, final @Nonnull String name, final boolean isAddPrefix, final boolean isAllowToString) {
            this.clazz = clazz;
            this.name = name;
            this.isAddPrefix = isAddPrefix;
            this.isAllowToString = isAllowToString;
        }

        @Override
        public Object invoke (final Object proxy, final Method method, final Object[] args) throws Throwable {

            if (method.getDeclaringClass() == Object.class) {
                if (this.isAllowToString) {
                    return method.invoke(this, args);
                } else {
                    throw new IllegalStateException("meta-method '" + method + "' unsupported for '" + this.clazz + "'");
                }
            }

            final Name ann = method.getAnnotation(Name.class);

            if (ann == null) {
                throw new ConfigurationException("method '" + method + "' must be annotated by '" + Name.class + "'");
            }

            final Class<?> methodType = method.getReturnType();
            final String newName = this.isAddPrefix ? this.name + ann.value() : ann.value();
            return createProxy(methodType, newName, true, Field.class.isAssignableFrom(methodType));

        }

        @Override
        public String toString () {
            return this.name;
        }

    }

    private EntityMeta () {
    }

}