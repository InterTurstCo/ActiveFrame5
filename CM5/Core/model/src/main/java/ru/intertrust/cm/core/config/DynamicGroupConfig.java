package ru.intertrust.cm.core.config;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.ElementListUnion;

public class DynamicGroupConfig implements Serializable {
    @Attribute(required = true)
    private String name;
    
    @Element(required = false)
    private ContextConfig context;
    
    @ElementListUnion({
        @ElementList(entry = "trackObjects", type = BusinessObjectTrackerConfig.class, inline = true)
    })
    private List<BusinessObjectTrackerConfig> trackers;
    
    @ElementList(entry = "includeGroup", inline = true)
    private List<NestedGroupConfig> nestedGroups;
}
