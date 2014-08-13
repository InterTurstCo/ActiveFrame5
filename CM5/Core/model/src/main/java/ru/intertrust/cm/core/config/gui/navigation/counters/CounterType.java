package ru.intertrust.cm.core.config.gui.navigation.counters;

/**
 * Created by
 * Bondarchuk Yaroslav
 * 03.08.2014
 * 11:07
 */

public enum CounterType {
    NON_READ("non-read"),
    ALL("all");

    private final String value;

    CounterType(String v)
    {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CounterType fromValue(String v) {
        for (CounterType c: CounterType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
