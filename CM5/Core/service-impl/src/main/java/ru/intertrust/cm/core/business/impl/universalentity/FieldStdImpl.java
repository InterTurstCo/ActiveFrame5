package ru.intertrust.cm.core.business.impl.universalentity;

import javax.annotation.Nonnull;

import ru.intertrust.cm.core.business.api.dto.universalentity.Field;
import ru.intertrust.cm.core.util.Args;

class FieldStdImpl<T> implements Field<T> {

    private static final long serialVersionUID = 1L;

    private final Class<T> clazz;
    private final boolean isAllowNull;
    private final DomainObjectContainer cnt;
    private final String fName;

    FieldStdImpl (final @Nonnull Class<T> clazz,
                  final boolean isAllowNull,
                  final @Nonnull DomainObjectContainer cnt,
                  final @Nonnull String fName) {

        this.clazz = Args.notNull(clazz, "clazz");
        this.isAllowNull = isAllowNull;
        this.cnt = Args.notNull(cnt, "cnt");
        this.fName = Args.notNull(fName, "fName");

    }

    @Override
    public T get () {

        final T value = TypesSupport.fromRdbmsValue(this.cnt.getDomainObject().getValue(this.fName), this.clazz);

        if (value == null && !this.isAllowNull) {
            throw new IllegalStateException("null-value detected but not allowed for this field");
        }

        return value;

    }

    @Override
    public void set (final T value) {

        if (value == null && !this.isAllowNull) {
            throw new IllegalArgumentException("null-value not allowed for this field");
        }

        this.cnt.getDomainObject().setValue(this.fName, TypesSupport.fromJavaValue(value, this.clazz));

    }

}