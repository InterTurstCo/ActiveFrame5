package ru.intertrust.cm.core.config;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

public class BusinessObjectTrackerConfig implements Serializable {
    @Attribute
    private String type;
    
    @Attribute
    private String status;
    
    @Element
    private String contextObject;
    
    @Element
    private String person;
}
