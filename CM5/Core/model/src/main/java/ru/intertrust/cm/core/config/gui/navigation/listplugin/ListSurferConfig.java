package ru.intertrust.cm.core.config.gui.navigation.listplugin;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;


/**
 * Created by Ravil on 10.04.2017.
 */
@Root(name = "list-surfer")
public class ListSurferConfig extends PluginConfig {

    private static final String COMPONENT_NAME = "list.surfer.plugin";
    @Element(name = "list-plugin")
    private ListPluginConfig listPluginConfig;

    @Element(name = "form-viewer", required = false)
    private FormViewerConfig formViewerConfig;

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public ListSurferConfig(){}

    public ListPluginConfig getListPluginConfig() {
        return listPluginConfig;
    }

    public void setListPluginConfig(ListPluginConfig listPluginConfig) {
        this.listPluginConfig = listPluginConfig;
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
        ListSurferConfig that = (ListSurferConfig) o;
        if (listPluginConfig != null
                ? !listPluginConfig.equals(that.listPluginConfig)
                : that.listPluginConfig != null) {
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
        int result = listPluginConfig != null ? listPluginConfig.hashCode() : 31;
        result = 31 * result + (formViewerConfig != null ? formViewerConfig.hashCode() : 31);
        return result;
    }
}
