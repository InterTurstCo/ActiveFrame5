package ru.intertrust.cm.core.business.api.dto;

/**
 * Перечисление типов полей доменных объектов, реализованных в системе.
 *
 * @author apirozhkov
 */
public enum FieldType {

    STRING(StringValue.class),
    LONG(LongValue.class),
    BOOLEAN(BooleanValue.class),
    DECIMAL(DecimalValue.class),
    DATETIMEWITHTIMEZONE(DateTimeWithTimeZoneValue.class),
    DATETIME(TimestampValue.class),
    TIMELESSDATE(TimelessDateValue.class),
    REFERENCE(ReferenceValue.class),
    PASSWORD(StringValue.class);

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
     * @throws NullPointerException если параметр равен null
     */
    public static FieldType find(Class<? extends Value> clazz) {
        for (FieldType type : FieldType.class.getEnumConstants()) {
            if (clazz.equals(type.getValueClass())) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown field value class: " + clazz.getName());
    }
}
