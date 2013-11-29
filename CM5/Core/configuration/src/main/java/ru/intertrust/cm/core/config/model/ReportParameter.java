package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;


public class ReportParameter {
    @Attribute
    private String name;
    
    @Element
    private ReportParametrSettings settings;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ReportParametrSettings getSettings() {
        return settings;
    }

    public void setSettings(ReportParametrSettings settings) {
        this.settings = settings;
    }
}
