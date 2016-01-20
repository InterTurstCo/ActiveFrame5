package ru.intertrust.cm.core.dao.impl.utils;

import java.sql.Types;

/**
 * Перечисление типов параметров PreparedStatement
 */
public enum ParameterType {
    /**
     Для PostgreSQL:
     bool ==> -7
     bytea ==> -2
     char ==> 1
     name ==> 12
     int8 (bigint) ==> -5
     bigserial ==> -5
     int2 (smallint) ==> 5
     int4 (integer) ==> 4
     serial ==> 4
     text ==> 12
     oid ==> -5
     float4 ==> 7
     float8 ==> 8
     money ==> 8
     bpchar ==> 1
     varchar ==> 12
     date ==> 91
     time ==> 92
     timestamp ==> 93
     timestamptz ==> 93
     timetz ==> 92
     bit ==> -7
     numeric ==> 2
     */
    LONG(Types.BIGINT),
    REFERENCE(Types.BIGINT),
    REFERENCE_TYPE(Types.INTEGER),
    DATETIME(Types.TIMESTAMP),
    STRING(Types.VARCHAR),
    TEXT(Types.VARCHAR),
    BOOLEAN(Types.SMALLINT),
    DECIMAL(Types.NUMERIC);

    private int sqlType;

    ParameterType(int sqlType) {
        this.sqlType = sqlType;
    }

    public int getSqlType() {
        return sqlType;
    }
}
