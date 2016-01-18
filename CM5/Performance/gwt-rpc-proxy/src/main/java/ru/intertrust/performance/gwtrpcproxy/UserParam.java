package ru.intertrust.performance.gwtrpcproxy;

import org.simpleframework.xml.Attribute;

public class UserParam {
    @Attribute
    private String name;
    @Attribute
    private String value;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    
    
}
