package ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 26.07.2016
 * Time: 10:41
 * To change this template use File | Settings | File and Code Templates.
 */
@Root(name = "hierarchy-plugin")
public class HierarchyPluginConfig extends PluginConfig {
    private static final String COMPONENT_NAME = "hierarchy.plugin";

    @Element(name = "hierarchy-group",required = true)
    private HierarchyGroupConfig hierarchyGroupConfig;

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public HierarchyGroupConfig getHierarchyGroupConfig() {
        return hierarchyGroupConfig;
    }

    public void setHierarchyGroupConfig(HierarchyGroupConfig hierarchyGroupConfig) {
        this.hierarchyGroupConfig = hierarchyGroupConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HierarchyPluginConfig that = (HierarchyPluginConfig) o;
        if (hierarchyGroupConfig != null
                ? !hierarchyGroupConfig.equals(that.hierarchyGroupConfig)
                : that.hierarchyGroupConfig != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = hierarchyGroupConfig != null ? hierarchyGroupConfig.hashCode() : 31;
        return result;
    }
    public HierarchyPluginConfig(){}
}
