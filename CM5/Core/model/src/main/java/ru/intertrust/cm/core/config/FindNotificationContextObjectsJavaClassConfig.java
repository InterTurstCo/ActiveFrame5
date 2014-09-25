package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.convert.Convert;

import ru.intertrust.cm.core.config.converter.FindObjectSettingsConverter;

public class FindNotificationContextObjectsJavaClassConfig extends FindNotificationContextObjectsConfig {
    @Attribute(required = false, name = "name")
    private String className;
    
    @Element(required = false, name = "settings")
    @Convert(FindObjectSettingsConverter.class)
    private FindObjectSettings settings;    

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public FindObjectSettings getSettings() {
        return settings;
    }

    public void setSettings(FindObjectSettings settings) {
        this.settings = settings;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((className == null) ? 0 : className.hashCode());
        result = prime * result + ((settings == null) ? 0 : settings.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FindNotificationContextObjectsJavaClassConfig other = (FindNotificationContextObjectsJavaClassConfig) obj;
        if (className == null) {
            if (other.className != null)
                return false;
        } else if (!className.equals(other.className))
            return false;
        if (settings == null) {
            if (other.settings != null)
                return false;
        } else if (!settings.equals(other.settings))
            return false;
        return true;
    }

}
