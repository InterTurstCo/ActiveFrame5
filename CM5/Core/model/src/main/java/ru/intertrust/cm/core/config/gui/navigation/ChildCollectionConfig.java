package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 10.09.2015
 */
@Root(name = "child-collection")
public class ChildCollectionConfig extends PluginConfig {

    @Attribute(name = "name", required = true)
    private String name;

    @Element(name = "initial-filters", required = false)
    private InitialFiltersConfig initialFiltersConfig;

    @Override
    public String getComponentName() {
        return "collection.plugin";
    }

    public InitialFiltersConfig getInitialFiltersConfig() {
        return initialFiltersConfig;
    }

    public void setInitialFiltersConfig(InitialFiltersConfig initialFiltersConfig) {
        this.initialFiltersConfig = initialFiltersConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ChildCollectionConfig that = (ChildCollectionConfig) o;

        if (initialFiltersConfig != null ? !initialFiltersConfig.equals(that.initialFiltersConfig) : that.initialFiltersConfig != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = initialFiltersConfig != null ? initialFiltersConfig.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
