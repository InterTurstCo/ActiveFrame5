package ru.intertrust.cm.core.business.api.dto;

public abstract class SearchFilterBase implements SearchFilter {

    private String fieldName;

    public SearchFilterBase() {
    }

    public SearchFilterBase(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
