package ru.intertrust.cm.core.gui.model.csv;

/**
 * Created by Vitaliy Orlov on 23.08.2016.
 */
public class JsonSearchQueryFilterValue {
    private String propertyName;
    private Object propertyValue;

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Object getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(Object propertyValue) {
        this.propertyValue = propertyValue;
    }
}
