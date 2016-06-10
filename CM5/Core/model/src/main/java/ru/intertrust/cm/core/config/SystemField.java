package ru.intertrust.cm.core.config;

import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;

/**
 * Перечень системных полей с указанием типа поля
 * (!) Enum.name должен соответствовать названию поля таблицы
 */
public enum SystemField {

    id(ReferenceFieldConfig.class),
    created_date(DateTimeFieldConfig.class),
    updated_date(DateTimeFieldConfig.class),
    status(ReferenceFieldConfig.class, GenericDomainObject.STATUS_DO),
    created_by(ReferenceFieldConfig.class, GenericDomainObject.PERSON_DOMAIN_OBJECT),
    updated_by(ReferenceFieldConfig.class, GenericDomainObject.PERSON_DOMAIN_OBJECT),
    access_object_id(ReferenceFieldConfig.class);

    private Class<? extends FieldConfig> fieldConfigClass;
    private String referenceType;

    SystemField(Class<? extends FieldConfig> fieldConfigClass) {
        this.fieldConfigClass = fieldConfigClass;
    }

    SystemField(Class<? extends FieldConfig> fieldConfigClass, String referenceType) {
        this.fieldConfigClass = fieldConfigClass;
        this.referenceType = referenceType;
    }

    public Class<? extends FieldConfig> getFieldConfigClass() {
        return fieldConfigClass;
    }

    public String getReferenceType() {
        return referenceType;
    }

    /**
     * Определяет, является ли поле системным
     * @param fieldConfig имя поле
     * @return true, если поле системное
     */
    public static boolean isSystemField(String fieldName) {
        try {
            SystemField.valueOf(fieldName.toLowerCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
