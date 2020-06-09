package ru.intertrust.cm.core.business.api.simpledata;

public class SimpleSearchOrder {
    private String fieldName;
    private SumpleSearchOrderDirection direction;

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public SumpleSearchOrderDirection getDirection() {
        return direction;
    }

    public void setDirection(SumpleSearchOrderDirection direction) {
        this.direction = direction;
    }
}
