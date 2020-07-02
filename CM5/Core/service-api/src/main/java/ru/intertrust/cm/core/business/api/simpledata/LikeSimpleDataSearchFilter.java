package ru.intertrust.cm.core.business.api.simpledata;

import ru.intertrust.cm.core.business.api.dto.Value;

public class LikeSimpleDataSearchFilter  implements SimpleDataSearchFilter {
    private String fieldName;
    private Value fieldValue;

    public LikeSimpleDataSearchFilter() {
    }

    public LikeSimpleDataSearchFilter(String fieldName, Value fieldValue) {
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Value getFieldValue() {
        return fieldValue;
    }

    public void setFieldValue(Value fieldValue) {
        this.fieldValue = fieldValue;
    }
}
