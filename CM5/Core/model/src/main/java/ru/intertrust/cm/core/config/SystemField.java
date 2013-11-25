package ru.intertrust.cm.core.config;

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
