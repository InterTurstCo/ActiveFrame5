package ru.intertrust.cm.core.config.model;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class NestedGroupConfig implements Serializable {
    @Attribute
    private String name;
    
    @Element
    private String contextObject;
}
