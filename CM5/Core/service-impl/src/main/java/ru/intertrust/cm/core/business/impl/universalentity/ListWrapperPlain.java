package ru.intertrust.cm.core.business.impl.universalentity;

import java.util.Objects;

import javax.annotation.Nonnull;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

class ListWrapperPlain<E> extends ListWrapper<E> {

    private static final String FIELD_VALUE = "Value";

    ListWrapperPlain (final @Nonnull String odopName, final @Nonnull Class<E> clazz, final boolean isAllowNulls, final @Nonnull DomainObjectContainer cnt) {
        super(odopName, clazz, isAllowNulls, cnt);
    }

    @Override
    E fromOdop (final DomainObject odop) {
        return TypesSupport.fromRdbmsValue(odop.getValue(FIELD_VALUE), this.clazz);
    }

    @Override
    boolean isAllFieldsEquals (final DomainObject odop, final E currState) {
        return Objects.equals(TypesSupport.fromRdbmsValue(odop.getValue(FIELD_VALUE), this.clazz), currState);
    }

    @Override
    boolean isUniqueConflicts (final DomainObject odop, final E currState) {
        return this.isAllFieldsEquals(odop, currState);
    }

    @Override
    void toOdop (final DomainObject odop, final E currState) {
        odop.setValue(FIELD_VALUE, TypesSupport.fromJavaValue(currState, this.clazz));
    }

}