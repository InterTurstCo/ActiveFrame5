package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.convert.Convert;
import ru.intertrust.cm.core.config.converter.FindObjectSettingsConverter;

public class FindObjectsClassConfig implements FindObjectsType {

    @Element(name = "find-settings", required = false)
    @Convert(FindObjectSettingsConverter.class)
    private FindObjectSettings settings;

    @Attribute(required = true, name = "name")
    private String className;

    public FindObjectSettings getSettings() {
        return settings;
    }

    public void setSettings(FindObjectSettings settings) {
        this.settings = settings;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    @Override
    public String getData() {
        return className;
    }

    @Override
    public void setData(String data) {
        this.className = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FindObjectsClassConfig that = (FindObjectsClassConfig) o;

        if (settings != null ? !settings.equals(that.settings) : that.settings != null) return false;
        if (className != null ? !className.equals(that.className) : that.className != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return className != null ? className.hashCode() : 0;
    }
}
