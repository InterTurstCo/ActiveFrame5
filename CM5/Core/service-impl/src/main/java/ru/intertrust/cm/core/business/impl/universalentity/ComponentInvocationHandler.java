package ru.intertrust.cm.core.business.impl.universalentity;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import ru.intertrust.cm.core.business.api.dto.universalentity.Field;
import ru.intertrust.cm.core.business.api.dto.universalentity.ListElement;
import ru.intertrust.cm.core.business.api.dto.universalentity.Name;
import ru.intertrust.cm.core.util.Args;
import ru.intertrust.cm.core.util.ReflectUtils;

class ComponentInvocationHandler implements InvocationHandler, Serializable {

    private static final long serialVersionUID = 1L;

    @Nonnull
    static <X> X createProxy (final @Nonnull Class<X> clazz, final @Nonnull String prefix, final @Nonnull DomainObjectContainer cnt) {
        return clazz.cast(Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new ComponentInvocationHandler(prefix, cnt)));
    }

    private final String prefix;
    protected final DomainObjectContainer cnt;
    private final Map<String, Object> methodValues = new HashMap<>();

    ComponentInvocationHandler (final @Nonnull String prefix, final @Nonnull DomainObjectContainer cnt) {
        this.prefix = Args.notNull(prefix, "prefix");
        this.cnt = Args.notNull(cnt, "cnt");
    }

    @Override
    public final Object invoke (final Object proxy, final Method method, final Object[] args) throws Throwable {

        if (this.isInvokeOnSelf(method.getDeclaringClass())) {
            return method.invoke(this, args);
        }

        final String methodName = method.getName();
        Object result = this.methodValues.get(methodName);

        if (result == null) {
            this.methodValues.put(methodName, result = this.createValue(method));
        }

        return result;

    }

    protected boolean isInvokeOnSelf (final @Nonnull Class<?> declaringClazz) {
        return declaringClazz == Object.class;
    }

    protected void onAfterSave () {
        for (final Object mv : this.methodValues.values()) {
            if (mv instanceof ListWrapper<?>) {
                ((ListWrapper<?>)mv).update();
            }
        }
    }

    @Nonnull
    private Object createValue (final @Nonnull Method method) {

        final Class<?> methodType = method.getReturnType();
        final Name ann = method.getAnnotation(Name.class);
        final String fieldName = ann == null ? null : ann.value();
        final String fieldFullName = fieldName == null ? null : this.prefix + fieldName;

        if (methodType == Field.class) {
            return new FieldStdImpl<>(ReflectUtils.getGenericReturnTypeParam(method), method.isAnnotationPresent(Nonnull.class), this.cnt, fieldFullName);
        } else if (methodType == List.class) {
            final Class<?> listType = ReflectUtils.getGenericReturnTypeParam(method);
            return ListElement.class.isAssignableFrom(listType) ? new ListWrapperLe<>(listType, this.cnt)
                    : new ListWrapperPlain<>(fieldName, listType, method.isAnnotationPresent(Nonnull.class), this.cnt);
        } else { // компонент
            return createProxy(methodType, fieldFullName, this.cnt);
        }

    }

}