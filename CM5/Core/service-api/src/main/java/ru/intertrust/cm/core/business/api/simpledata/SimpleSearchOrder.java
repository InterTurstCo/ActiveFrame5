package ru.intertrust.cm.core.business.api.simpledata;

import ru.intertrust.cm.core.business.api.dto.Dto;

public class SimpleSearchOrder implements Dto {
    private String fieldName;
    private SumpleSearchOrderDirection direction;

    public SimpleSearchOrder() {
    }

    public SimpleSearchOrder(String fieldName, SumpleSearchOrderDirection direction) {
        this.fieldName = fieldName;
        this.direction = direction;
    }

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
