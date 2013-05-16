package ru.intertrust.cm.core.config;

/**
* @author vmatsukevich
*         Date: 5/15/13
*         Time: 8:15 PM
*/
public enum FieldType {
    DATE_TIME, DECIMAL, LONG, REFERENCE, STRING;

    public static FieldType getType(FieldConfig fieldConfig) {
        if(DateTimeFieldConfig.class.equals(fieldConfig.getClass())) {
            return DATE_TIME;
        }

        if(DecimalFieldConfig.class.equals(fieldConfig.getClass())) {
            return DECIMAL;
        }

        if(LongFieldConfig.class.equals(fieldConfig.getClass())) {
            return LONG;
        }

        if(ReferenceFieldConfig.class.equals(fieldConfig.getClass())) {
            return REFERENCE;
        }

        if(StringFieldConfig.class.equals(fieldConfig.getClass()) ||
                PasswordFieldConfig.class.equals(fieldConfig.getClass())) {
            return STRING;
        }

        throw new IllegalArgumentException("Invalid field type");
    }
}
