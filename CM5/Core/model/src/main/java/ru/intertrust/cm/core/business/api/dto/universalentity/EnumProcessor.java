package ru.intertrust.cm.core.business.api.dto.universalentity;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

public abstract class EnumProcessor<E> {

    private final Map<E, Long> m1 = new HashMap<>();
    private final Map<Long, E> m2 = new HashMap<>();

    protected final void add (final @Nonnull E enumValue, final @Nonnull Long dbValue) {
        if (this.m1.put(enumValue, dbValue) != null) {
            throw new ConfigurationException("enumValue '" + enumValue + "' already exist.");
        } else if (this.m2.put(dbValue, enumValue) != null) {
            throw new ConfigurationException("dbValue '" + dbValue + "' already exist.");
        }
    }

    public final E fromLongValue (final Long dbValue) {

        if (dbValue == null) {
            return null;
        }

        final E result = this.m2.get(dbValue);

        if (result == null) {
            throw new RuntimeException("unknown dbValue '" + dbValue + "'");
        } else {
            return result;
        }

    }

    public final Long fromEnumValue (final E enumValue) {

        if (enumValue == null) {
            return null;
        }

        final Long result = this.m1.get(enumValue);

        if (result == null) {
            throw new RuntimeException("unknown enumValue '" + enumValue + "'");
        } else {
            return result;
        }

    }

}