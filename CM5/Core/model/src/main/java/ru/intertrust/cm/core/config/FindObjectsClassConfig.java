package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.convert.Convert;

import ru.intertrust.cm.core.config.converter.FindObjectSettingsConverter;

public class FindObjectsClassConfig implements FindObjectsType {

    @Element(name = "find-person-settings", required = false)
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
}
