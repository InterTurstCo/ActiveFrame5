package ru.intertrust.cm.core.wsserver.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiParam;
import ru.intertrust.cm.core.business.api.dto.FieldType;

@ApiModel(subTypes = FieldData.class)
public class ValueData {
    private FieldType type;
    private String value;

    public ValueData(){
    }

    public ValueData(FieldType type, String value) {
        this.type = type;
        this.value = value;
    }

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
