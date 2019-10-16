package ru.intertrust.cm.core.business.api.dto;

import ru.intertrust.cm.core.business.api.dto.util.ListValue;
import ru.intertrust.cm.core.business.api.dto.util.ModelConstants;

/**
 * Перечисление типов полей доменных объектов, реализованных в системе.
 *
 * @author apirozhkov
 */
public enum FieldType {
    STRING(StringValue.class),
    TEXT(StringValue.class),
    LONG(LongValue.class),
    BOOLEAN(BooleanValue.class),
    DECIMAL(DecimalValue.class),
    DATETIMEWITHTIMEZONE(DateTimeWithTimeZoneValue.class),
    DATETIME(DateTimeValue.class),
    TIMELESSDATE(TimelessDateValue.class),
    REFERENCE(ReferenceValue.class),
    @Deprecated
    PASSWORD(StringValue.class),
    LIST(ListValue.class);

    private Class<? extends Value> valueClass;

    private FieldType(Class<? extends Value> valueClass) {
        this.valueClass = valueClass;
    }

    /**
     * Возвращает класс, предназначенный для хранения значений поля данного типа.
     *
     * @return Класс &mdash; потомок {@link Value}
     */
    public Class<? extends Value> getValueClass() {
        return valueClass;
    }

    /**
     * Определяет тип поля по классу, предназначенному для хранения значений поля.
     *
     * @param clazz Класс &mdash; потомок {@link Value}
     * @return константа, определяющая тип поля
     * @throws IllegalArgumentException если для заданного класса не определена константа типа поля
     * @throws NullPointerException     если параметр равен null
     */
    public static FieldType find(Class<? extends Value> clazz) {
        for (FieldType type : FieldType.class.getEnumConstants()) {
            if (clazz.equals(type.getValueClass())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown field value class: " + clazz.getName());
    }

    public static FieldType forTypeName(String typeName) {
        switch (typeName) {
            case ModelConstants.STRING_TYPE:
                return STRING;
            case ModelConstants.TEXT_TYPE:
                return TEXT;
            case ModelConstants.LONG_TYPE:
                return LONG;
            case ModelConstants.BOOLEAN_TYPE:
                return BOOLEAN;
            case ModelConstants.DECIMAL_TYPE:
                return DECIMAL;
            case ModelConstants.DATE_TIME_WITH_TIME_ZONE_TYPE:
                return DATETIMEWITHTIMEZONE;
            case ModelConstants.DATE_TIME_TYPE:
                return DATETIME;
            case ModelConstants.TIMELESS_DATE_TYPE:
                return TIMELESSDATE;
            case ModelConstants.REFERENCE_TYPE:
                return REFERENCE;
            case ModelConstants.PSSWRD_TYPE:
                return PASSWORD;
            case ModelConstants.LIST_TYPE:
                return LIST;
            default:
                throw new IllegalArgumentException("Unsupported Field Type: " + typeName);
        }

    }
}
