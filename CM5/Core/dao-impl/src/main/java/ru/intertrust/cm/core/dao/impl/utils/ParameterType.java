package ru.intertrust.cm.core.dao.impl.utils;

import java.sql.Types;

/**
 * Перечисление типов параметров PreparedStatement
 */
public enum ParameterType {
    NUMERIC(Types.NUMERIC), DATE(Types.DATE), STRING(Types.VARCHAR), DECIMAL(Types.DECIMAL);

    private int sqlType;

    ParameterType(int sqlType) {
        this.sqlType = sqlType;
    }

    public int getSqlType() {
        return sqlType;
    }
}
