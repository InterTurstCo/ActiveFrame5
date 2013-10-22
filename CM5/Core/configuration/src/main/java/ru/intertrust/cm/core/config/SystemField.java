package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.config.model.DateTimeFieldConfig;
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;

/**
 * Перечень системных полей с указанием типа поля
 * (!) Enum.name должен соответствовать названию поля таблицы
 */
public enum SystemField {

    id(ReferenceFieldConfig.class),
    created_date(DateTimeFieldConfig.class),
    updated_date(DateTimeFieldConfig.class),
    status(ReferenceFieldConfig.class);

    private Class<? extends FieldConfig> fieldConfigClass;

    SystemField(Class<? extends FieldConfig> fieldConfigClass) {
        this.fieldConfigClass = fieldConfigClass;
    }

    public Class<? extends FieldConfig> getFieldConfigClass() {
        return fieldConfigClass;
    }


}
