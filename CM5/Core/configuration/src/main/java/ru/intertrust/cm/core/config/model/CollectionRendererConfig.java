package ru.intertrust.cm.core.config.model;

import org.simpleframework.xml.Attribute;

import java.io.Serializable;

public class CollectionRendererConfig implements Serializable {

    @Attribute(name = "className", required = true)
    private String className;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

}
