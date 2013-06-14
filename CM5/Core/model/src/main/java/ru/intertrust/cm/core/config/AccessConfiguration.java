package ru.intertrust.cm.core.config;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root
public class AccessConfiguration implements Serializable {
    @ElementList(entry = "staticGroup", type = StaticGroupConfig.class, inline = true)
    private List<DynamicGroupConfig> staticGroups;
    
    @ElementList(entry = "dynamicGroup", type = DynamicGroupConfig.class, inline = true)
    private List<DynamicGroupConfig> dynamicGroups;
    
    @ElementList(entry = "contextRole", type = ContextRoleConfig.class, inline = true)
    private List<ContextRoleConfig> contextRoles;
}
