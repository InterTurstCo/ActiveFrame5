package ru.intertrust.cm.core.config.converter;

import org.simpleframework.xml.transform.Transform;

@SuppressWarnings("rawtypes")
public class EnumTransform implements Transform<Enum> {

    private Class<? extends Enum> type;
    private String[] names;

    public EnumTransform(Class<? extends Enum> type) {
        this.type = type;
        Enum[] values = type.getEnumConstants();
        names = new String[values.length];
        for (int i = 0; i < names.length; ++i) {
            names[i] = values[i].name().toLowerCase().replace('_', '-');
        }
    }

    @Override
    public Enum read(String value) throws Exception {
        if (value == null || value.isEmpty()) {
            return null;
        }
        for (int i = 0; i < names.length; ++i) {
            if (value.equals(names[i])) {
                return type.getEnumConstants()[i];
            }
        }
        for (int i = 0; i < names.length; ++i) {
            if (value.trim().equalsIgnoreCase(names[i])) {
                return type.getEnumConstants()[i];
            }
        }
        return null;
    }

    @Override
    public String write(Enum value) throws Exception {
        if (value == null) {
            return null;
        }
        return names[value.ordinal()];
    }
}
