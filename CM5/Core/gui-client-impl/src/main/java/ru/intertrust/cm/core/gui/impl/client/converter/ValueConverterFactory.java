package ru.intertrust.cm.core.gui.impl.client.converter;

import ru.intertrust.cm.core.business.api.dto.FieldType;

/**
 * @author Sergey.Okolot
 *         Created on 21.01.14 11:04.
 */
public final class ValueConverterFactory {

    private static final ValueConverter DEFAULT = new DefaultValueConverter();

    private ValueConverterFactory() {
    }

    public static ValueConverter getConverter(String fieldType) {
        if ("integer".equals(fieldType)) {
            fieldType = "long";
        }
        final FieldType type = FieldType.valueOf(fieldType.toUpperCase());
        switch (type) {
            case BOOLEAN:
                return new BooleanValueConverter();
            case DATETIME:
                return new DateTimeConverter();
            default:
                return DEFAULT;
        }
    }
}
