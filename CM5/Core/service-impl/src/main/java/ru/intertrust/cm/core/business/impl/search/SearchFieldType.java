package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.FieldType;

public enum SearchFieldType {

    TEXT("t_"),
    DATE("dt_"),
    LONG("l_"),
    REF ("r_"),
    BOOL("b_"),
    TEXT_MULTI("ts_"),
    DATE_MULTI("dts_"),
    LONG_MULTI("ls_"),
    REF_MULTI ("rs_"),
    BOOL_MULTI("bs_");

    private String infix;

    private SearchFieldType(String infix) {
        this.infix = infix;
    }

    public String getInfix() {
        return infix;
    }

    public static SearchFieldType getFieldType(FieldType type, boolean multiValued) {
        if (FieldType.DATETIME == type ||
            FieldType.DATETIMEWITHTIMEZONE == type ||
            FieldType.TIMELESSDATE == type) {
            return multiValued ? DATE_MULTI : DATE;
        }
        if (FieldType.LONG == type ||
            FieldType.DECIMAL == type) {
            return multiValued ? LONG_MULTI : LONG;
        }
        if (FieldType.REFERENCE == type) {
            return multiValued ? REF_MULTI : REF;
        }
        if (FieldType.BOOLEAN == type) {
            return multiValued ? BOOL_MULTI : BOOL;
        }
        return multiValued ? TEXT_MULTI : TEXT;
    }

}
