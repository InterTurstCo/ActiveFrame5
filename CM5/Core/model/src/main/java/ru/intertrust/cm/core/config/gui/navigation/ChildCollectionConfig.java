package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.widget.filter.extra.CollectionExtraFiltersConfig;

/**
 * @author Ravil Abdulkhairov
 * @version 1.0
 * @since 10.09.2015
 */
@Root(name = "child-collection")
public class ChildCollectionConfig extends PluginConfig {

    @Attribute(name = "name", required = true)
    private String name;

    @Element(name = "collection-extra-filters", required = false)
    private CollectionExtraFiltersConfig collectionExtraFiltersConfig;

    @Override
    public String getComponentName() {
        return "collection.plugin";
    }

    public CollectionExtraFiltersConfig getCollectionExtraFiltersConfig() {
        return collectionExtraFiltersConfig;
    }

    public void setCollectionExtraFiltersConfig(CollectionExtraFiltersConfig collectionExtraFiltersConfig) {
        this.collectionExtraFiltersConfig = collectionExtraFiltersConfig;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

        if (collectionExtraFiltersConfig != null ? !collectionExtraFiltersConfig.equals(that.collectionExtraFiltersConfig) : that.collectionExtraFiltersConfig != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = collectionExtraFiltersConfig != null ? collectionExtraFiltersConfig.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
