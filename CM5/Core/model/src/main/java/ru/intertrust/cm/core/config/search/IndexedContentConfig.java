package ru.intertrust.cm.core.config.search;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;

public class IndexedContentConfig implements Serializable {

    @Attribute(required = true)
    private String type;

    @Attribute(name = "show-in-results", required = false)
    private Boolean showInResults;

    public String getType() {
        return type;
    }

    public boolean getShowInResults() {
        return showInResults != null ? showInResults.booleanValue() : false;
    }

    @Override
    public int hashCode() {
        int hash = type.hashCode();
        hash = hash * 31 ^ (showInResults != null ? showInResults.hashCode() : 0);
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
               && (showInResults == null ? other.showInResults == null : showInResults.equals(other.showInResults));
    }
}
