package ru.intertrust.cm.core.gui.model.form.widget;


import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.HashMap;
import java.util.Map;

public class RowItem implements Dto {

    HashMap<String, String> values = new HashMap<String, String>();
    HashMap<String, String> additionalParams = new HashMap<String, String>();
    private Id objectId;
    private String domainObjectType;
    private Map<String, Boolean> accessMatrix;

    public String getValueByKey(String key) {
        return values.get(key);
    }

    public void setValueByKey(String key, String value) {
        values.put(key, value);
    }

    public void setObjectId(Id objectId) {
        this.objectId = objectId;
    }

    public Id getObjectId() {
        return objectId;
    }

    public void setParameter(String key, String value) {
        additionalParams.put(key, value);
    }

    public String getParameter(String key) {
        return additionalParams.get(key);
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public void setDomainObjectType(String domainObjectType) {
        this.domainObjectType = domainObjectType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RowItem rowItem = (RowItem) o;

        if (additionalParams != null ? !additionalParams.equals(rowItem.additionalParams)
                : rowItem.additionalParams != null) {
            return false;
        }

        if (objectId != null ? !objectId.equals(rowItem.objectId) : rowItem.objectId != null) {
            return false;
        }
        if (values != null ? !values.equals(rowItem.values) : rowItem.values != null) {
            return false;
        }
        if (domainObjectType != null ? !domainObjectType.equals(rowItem.domainObjectType) : rowItem.domainObjectType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = values != null ? values.hashCode() : 0;
        result = 31 * result + (additionalParams != null ? additionalParams.hashCode() : 0);
        result = 31 * result + (objectId != null ? objectId.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RowItem{" +
                "values=" + values +
                ", additionalParams=" + additionalParams +
                ", objectId=" + objectId +
                '}';
    }

    public void setAccessMatrix(Map<String, Boolean> accessMatrix) {
        this.accessMatrix = accessMatrix;
    }

    public Map<String, Boolean> getAccessMatrix() {
        return accessMatrix;
    }
}
