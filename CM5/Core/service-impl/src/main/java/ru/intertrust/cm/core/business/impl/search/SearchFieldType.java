package ru.intertrust.cm.core.business.impl.search;

import ru.intertrust.cm.core.business.api.dto.FieldType;

public enum SearchFieldType {

    TEXT("t_"),
    DATE("dt_"),
    LONG("l_"),
    DOUBLE("d_"),
    REF ("r_"),
    BOOL("b_"),
    TEXT_MULTI("ts_"),
    DATE_MULTI("dts_"),
    LONG_MULTI("ls_"),
    DOUBLE_MULTI("ds_"),
    REF_MULTI ("rs_"),
    BOOL_MULTI("bs_"),
    TEXT_SUBSTRING("t_"),
    TEXT_MULTI_SUBSTRING("ts_");

    public final String infix;

    private SearchFieldType(String infix) {
        this.infix = infix;
    }

    @Deprecated
    public String getInfix() {
        return infix;
    }

    public static SearchFieldType getFieldType(FieldType type, boolean multiValued) {
        switch(type) {
        case DATETIME:
        case DATETIMEWITHTIMEZONE:
        case TIMELESSDATE:
            return multiValued ? DATE_MULTI : DATE;
        case LONG:
            return multiValued ? LONG_MULTI : LONG;
        case DECIMAL:
            return multiValued ? DOUBLE_MULTI : DOUBLE;
        case REFERENCE:
            return multiValued ? REF_MULTI : REF;
        case BOOLEAN:
            return multiValued ? BOOL_MULTI : BOOL;
        default:
            return multiValued ? TEXT_MULTI : TEXT;
        }
    }

}
