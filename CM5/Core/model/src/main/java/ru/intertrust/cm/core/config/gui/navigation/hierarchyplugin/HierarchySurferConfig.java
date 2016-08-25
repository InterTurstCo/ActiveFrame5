package ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 25.08.2016
 * Time: 9:37
 * To change this template use File | Settings | File and Code Templates.
 */
@Root(name = "hierarchy-surfer")
public class HierarchySurferConfig extends PluginConfig {
    private static final String COMPONENT_NAME = "hierarchy.surfer.plugin";

    @Element(name = "hierarchy-plugin")
    private HierarchyPluginConfig hierarchyPluginConfig;

    @Element(name = "form-viewer", required = false)
    private FormViewerConfig formViewerConfig;

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public HierarchyPluginConfig getHierarchyPluginConfig() {
        return hierarchyPluginConfig;
    }

    public void setHierarchyPluginConfig(HierarchyPluginConfig hierarchyPluginConfig) {
        this.hierarchyPluginConfig = hierarchyPluginConfig;
    }

    public FormViewerConfig getFormViewerConfig() {
        return formViewerConfig;
    }

    public void setFormViewerConfig(FormViewerConfig formViewerConfig) {
        this.formViewerConfig = formViewerConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HierarchySurferConfig that = (HierarchySurferConfig) o;
        if (hierarchyPluginConfig != null
                ? !hierarchyPluginConfig.equals(that.hierarchyPluginConfig)
                : that.hierarchyPluginConfig != null) {
            return false;
        }
        if (formViewerConfig != null
                ? !formViewerConfig.equals(that.formViewerConfig)
                : that.formViewerConfig != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = hierarchyPluginConfig != null ? hierarchyPluginConfig.hashCode() : 31;
        result = 31 * result + (formViewerConfig != null ? formViewerConfig.hashCode() : 31);
        return result;
    }
}
