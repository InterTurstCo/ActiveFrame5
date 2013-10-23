package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;

public class DomainObjTypeConfig {

    @Attribute(required = true)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
