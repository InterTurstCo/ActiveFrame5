package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.config.*;

/**
 * Конвертирует ColumnInfo в конфигуцию поля типа ДО
 */
public class ColumnInfoConverter {

    public static <T extends FieldConfig> FieldConfig convert(ColumnInfo columnInfo, T fieldConfig) {
        if (fieldConfig instanceof LongFieldConfig) {
            LongFieldConfig longFieldConfig = new LongFieldConfig();
            setBasicAttributes(longFieldConfig, columnInfo);
            return longFieldConfig;
        } else if (fieldConfig instanceof DateTimeFieldConfig) {
            DateTimeFieldConfig dateTimeFieldConfig = new DateTimeFieldConfig();
            setBasicAttributes(dateTimeFieldConfig, columnInfo);
            return dateTimeFieldConfig;
        } else if (fieldConfig instanceof ReferenceFieldConfig) {
            ReferenceFieldConfig referenceFieldConfig = new ReferenceFieldConfig();
            setBasicAttributes(referenceFieldConfig, columnInfo);
            return referenceFieldConfig;
        } else if (fieldConfig instanceof PasswordFieldConfig) {
            PasswordFieldConfig passwordFieldConfig = new PasswordFieldConfig();
            setBasicAttributes(passwordFieldConfig, columnInfo);
            passwordFieldConfig.setLength(columnInfo.getLength());
            return passwordFieldConfig;
        } else if (fieldConfig instanceof TimelessDateFieldConfig) {
            TimelessDateFieldConfig timelessDateFieldConfig = new TimelessDateFieldConfig();
            setBasicAttributes(timelessDateFieldConfig, columnInfo);
            return timelessDateFieldConfig;
        } else if (fieldConfig instanceof DateTimeWithTimeZoneFieldConfig) {
            DateTimeWithTimeZoneFieldConfig timeWithTimeZoneFieldConfig = new DateTimeWithTimeZoneFieldConfig();
            setBasicAttributes(timeWithTimeZoneFieldConfig, columnInfo);
            return timeWithTimeZoneFieldConfig;
        } else if (fieldConfig instanceof DecimalFieldConfig) {
            DecimalFieldConfig decimalFieldConfig = new DecimalFieldConfig();
            setBasicAttributes(decimalFieldConfig, columnInfo);
            decimalFieldConfig.setPrecision(columnInfo.getPrecision());
            decimalFieldConfig.setScale(columnInfo.getScale());
            return decimalFieldConfig;
        } else if (fieldConfig instanceof TextFieldConfig) {
            TextFieldConfig textFieldConfig = new TextFieldConfig();
            setBasicAttributes(textFieldConfig, columnInfo);
            return textFieldConfig;
        } else if (fieldConfig instanceof BooleanFieldConfig) {
            BooleanFieldConfig booleanFieldConfig = new BooleanFieldConfig();
            setBasicAttributes(booleanFieldConfig, columnInfo);
            return booleanFieldConfig;
        } else if (fieldConfig instanceof StringFieldConfig) {
            StringFieldConfig stringFieldConfig = new StringFieldConfig();
            setBasicAttributes(stringFieldConfig, columnInfo);
            stringFieldConfig.setLength(columnInfo.getLength());
            return stringFieldConfig;
        }

        throw new IllegalArgumentException("Unknown type of field config '" + fieldConfig.getClass().getName() + "'");
    }

    private static void setBasicAttributes(FieldConfig fieldConfig, ColumnInfo columnInfo) {
        fieldConfig.setName(columnInfo.getName());
        fieldConfig.setNotNull(columnInfo.isNotNull());
    }
}
