package ru.intertrust.cm.core.dao.impl.sqlparser;

import java.util.Objects;
import ru.intertrust.cm.core.config.FieldConfig;

public class FieldData {

    private final FieldConfig fieldConfig;
    private final String doTypeName;

    private String columnName;

    public FieldData(FieldConfig fieldConfig, String doTypeName) {
        this.fieldConfig = fieldConfig;
        this.doTypeName = doTypeName;
    }

    public FieldConfig getFieldConfig() {
        return fieldConfig;
    }

    public String getDoTypeName() {
        return doTypeName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getColumnName() {
        return columnName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FieldData)) return false;
        final FieldData fieldData = (FieldData) o;
        return Objects.equals(getFieldConfig(), fieldData.getFieldConfig()) && Objects.equals(getDoTypeName(), fieldData.getDoTypeName()) && Objects.equals(getColumnName(), fieldData.getColumnName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFieldConfig(), getDoTypeName(), getColumnName());
    }
}
