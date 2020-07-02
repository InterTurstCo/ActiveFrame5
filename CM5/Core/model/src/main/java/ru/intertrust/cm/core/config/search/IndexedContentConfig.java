package ru.intertrust.cm.core.config.search;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;

public class IndexedContentConfig implements Serializable {

    @Attribute(required = true)
    private String type;

    @Attribute(name = "show-in-results", required = false)
    private Boolean showInResults;

    @Attribute(name = "parent-fk-field", required = false)
    private String parentFkField;

    public String getType() {
        return type;
    }

    public boolean getShowInResults() {
        return showInResults != null ? showInResults.booleanValue() : false;
    }

    public String getParentFkField() {
        return parentFkField != null && !parentFkField.isEmpty() ? parentFkField : "f_dp_rkkbase";
    }

    @Override
    public int hashCode() {
        int hash = type.hashCode();
        hash = hash * 31 ^ (showInResults != null ? showInResults.hashCode() : 0);
        hash = hash * 31 ^ (parentFkField != null ? parentFkField.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        IndexedContentConfig other = (IndexedContentConfig) obj;

        return type.equals(other.type)
               && (parentFkField == null ? other.parentFkField == null : parentFkField.equals(other.parentFkField))
               && (showInResults == null ? other.showInResults == null : showInResults.equals(other.showInResults));
    }

}
