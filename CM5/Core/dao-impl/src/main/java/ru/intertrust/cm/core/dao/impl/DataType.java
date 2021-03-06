package ru.intertrust.cm.core.dao.impl;

/**
 * Перечисление типов колонок в таблицах доменных объектов. Используется для удобства чтения полей доменных объектов.
 *
 * @author atsvetkov
 */
public enum DataType {
    STRING("string"), INTEGER("int"), DECIMAL("decimal"), DATETIME("datetime"), TIMELESSDATE("timelessdate"),
    BOOLEAN("boolean"), ID("id"), LONG("long");
    private final String value;

    DataType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
