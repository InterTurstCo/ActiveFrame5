package ru.intertrust.cm.core.wsserver.model;

import java.util.List;

public class DomainObjectData {
    private String id;
    private String type;
    private List<FieldData> fields;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<FieldData> getFields() {
        return fields;
    }

    public void setFields(List<FieldData> fields) {
        this.fields = fields;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
