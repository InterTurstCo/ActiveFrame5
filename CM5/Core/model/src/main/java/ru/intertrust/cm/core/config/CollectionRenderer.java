package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;

public class CollectionRenderer {

    @Attribute(name = "className", required = true)
    private String className;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }   
    
}
