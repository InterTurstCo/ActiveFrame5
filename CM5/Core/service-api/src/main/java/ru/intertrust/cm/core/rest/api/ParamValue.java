package ru.intertrust.cm.core.rest.api;

public class ParamValue {
    private String value;
    private ReportParam.ParamTypes type;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ReportParam.ParamTypes getType() {
        return type;
    }

    public void setType(ReportParam.ParamTypes type) {
        this.type = type;
    }
}
