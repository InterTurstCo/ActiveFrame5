package ru.intertrust.cm.core.business.impl.universalentity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.universalentity.Field;
import ru.intertrust.cm.core.business.api.dto.universalentity.Name;

class ListWrapperLe<E> extends ListWrapper<E> {

    ListWrapperLe (final @Nonnull Class<E> clazz, final @Nonnull DomainObjectContainer cnt) {
        super(ConfigurationSupport.getClassMeta(clazz).dopType, clazz, false, cnt);
    }

    @Nonnull
    E createElement () {
        return this.fromOdop(BeansHolder.get().crud.createDomainObject(this.odopName));

    }

    @Override
    E fromOdop (final DomainObject odop) {
        return ComponentInvocationHandler.createProxy(this.clazz, "", new DomainObjectContainer(odop));
    }

    @Override
    boolean isAllFieldsEquals (final DomainObject odop, final E currState) {

        for (final Map.Entry<String, Object> e : this.getValuesMap(currState).entrySet()) {
            if (!Objects.equals(TypesSupport.fromRdbmsValue(odop.getValue(e.getKey()), Object.class), e.getValue())) {
                return false;
            }
        }

        return true;

    }

    @Override
    boolean isUniqueConflicts (final DomainObject odop, final E currState) {

        final Map<String, Object> valuesMap = this.getValuesMap(currState);

        for (final Collection<String> ucs : ConfigurationSupport.getUniqueConstraints(this.clazz)) {

            boolean b = true;

            for (final String fn : ucs) {
                if (!Objects.equals(TypesSupport.fromRdbmsValue(odop.getValue(fn), Object.class), valuesMap.get(fn))) {
                    b = false;
                    break;
                }
            }

            if (b) {
                return true;
            }

        }

        return false;

    }

    @Override
    void toOdop (final DomainObject odop, final E currState) {
        for (final Map.Entry<String, Object> e : this.getValuesMap(currState).entrySet()) {
            odop.setValue(e.getKey(), TypesSupport.fromJavaValue(e.getValue(), Object.class));
        }
    }

    @Nonnull
    private Map<String, Object> getValuesMap (final @Nonnull E currState) { // <ИмяПоляДОП> :: <JavaЗначение>

        final Map<String, Object> result = new HashMap<>();

        for (final Method m : currState.getClass().getInterfaces()[0].getMethods()) {
            if (Field.class.isAssignableFrom(m.getReturnType())) {
                try {
                    final Field<?> fv = (Field<?>)m.invoke(currState);
                    final Name ann = m.getAnnotation(Name.class);
                    result.put(ann.value(), fv.get());
                } catch (final IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return result;

    }

}