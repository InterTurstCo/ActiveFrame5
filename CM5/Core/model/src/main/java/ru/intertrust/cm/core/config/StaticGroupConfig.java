package ru.intertrust.cm.core.config;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;

public class StaticGroupConfig implements Serializable {
    @Attribute
    private String name;
}
