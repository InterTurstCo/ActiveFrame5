package ru.intertrust.cm.core.config.gui.navigation.listplugin;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;

/**
 * Created by Ravil on 10.04.2017.
 */
@Root(name = "list-plugin")
public class ListPluginConfig extends PluginConfig  {
    private static final String COMPONENT_NAME = "list.plugin";

    @Attribute(name = "title", required = true)
    private String title;

    @Attribute(name = "show-toolbar", required = false)
    private Boolean showToolbar;

    @Attribute(name = "show-counter", required = false)
    private Boolean showCounter;

    @Element(name = "list-collection",required = true)
    private ListCollectionConfig listCollectionConfig;

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    public ListPluginConfig(){}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getShowToolbar() {
        return showToolbar;
    }

    public void setShowToolbar(Boolean showToolbar) {
        this.showToolbar = showToolbar;
    }

    public Boolean getShowCounter() {
        return showCounter;
    }

    public void setShowCounter(Boolean showCounter) {
        this.showCounter = showCounter;
    }

    public ListCollectionConfig getListCollectionConfig() {
        return listCollectionConfig;
    }

    public void setListCollectionConfig(ListCollectionConfig listCollectionConfig) {
        this.listCollectionConfig = listCollectionConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ListPluginConfig that = (ListPluginConfig) o;
        if (listCollectionConfig != null
                ? !listCollectionConfig.equals(that.listCollectionConfig)
                : that.listCollectionConfig != null) {
            return false;
        }
        if (title != null
                ? !title.equals(that.title)
                : that.title != null) {
            return false;
        }
        if (showCounter != null
                ? !showCounter.equals(that.showCounter)
                : that.showCounter != null) {
            return false;
        }
        if (showToolbar != null
                ? !showToolbar.equals(that.showToolbar)
                : that.showToolbar != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = listCollectionConfig != null ? listCollectionConfig.hashCode() : 31;
        result = 31 * result + (showCounter != null ? showCounter.hashCode() : 31);
        result = 31 * result + (showToolbar != null ? showToolbar.hashCode() : 31);
        result = 31 * result + (title != null ? title.hashCode() : 31);
        return result;
    }
}
