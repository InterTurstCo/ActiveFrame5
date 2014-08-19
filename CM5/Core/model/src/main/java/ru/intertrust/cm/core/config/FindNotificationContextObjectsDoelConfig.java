package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

public class FindNotificationContextObjectsDoelConfig extends FindNotificationContextObjectsConfig {
    @Attribute(required = false, name = "source-object")
    private String sourceObject;

    public String getSourceObject() {
        return sourceObject;
    }

    public void setSourceObject(String sourceObject) {
        this.sourceObject = sourceObject;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        FindNotificationContextObjectsDoelConfig that = (FindNotificationContextObjectsDoelConfig) o;

        if (sourceObject != null ? !sourceObject.equals(that.sourceObject) : that.sourceObject != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (sourceObject != null ? sourceObject.hashCode() : 0);
        return result;
    }
}
