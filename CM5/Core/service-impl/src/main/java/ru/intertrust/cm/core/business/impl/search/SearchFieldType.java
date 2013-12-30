package ru.intertrust.cm.core.business.impl.search;

public enum SearchFieldType {

    TEXT(""),
    DATE("_dt"),
    LONG("_l");

    private String suffix;

    private SearchFieldType(String suffix) {
        this.suffix = suffix;
    }

    public String getSuffix() {
        return suffix;
    }
}
