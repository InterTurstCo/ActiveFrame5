package ru.intertrust.cm.core.business.impl.search;

public enum SearchFieldType {

    TEXT("_t"),
    DATE("_dt"),
    LONG("_l"),
    TEXT_MULTI("_txt"),
    DATE_MULTI("_dts"),
    LONG_MULTI("_ls");

    private String suffix;

    private SearchFieldType(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }
}
