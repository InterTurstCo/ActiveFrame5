package ru.intertrust.cm.core.wsserver.model;

import io.swagger.annotations.ApiModel;
import ru.intertrust.cm.core.business.api.dto.FieldType;

@ApiModel(parent = ValueData.class)
public class FieldData extends ValueData {
    private String name;

    public FieldData() {
    }

    public FieldData(String name, FieldType type, String value) {
        super(type, value);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
