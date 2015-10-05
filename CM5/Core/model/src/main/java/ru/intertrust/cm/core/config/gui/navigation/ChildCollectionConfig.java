package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.config.gui.form.widget.ExpandableObjectsConfig;
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

    @Attribute(name = "group-object-type", required = false)
    private String groupObjectType;

    @Attribute(name = "element-object-type", required = false)
    private String elementObjectType;


    @Element(name = "collection-extra-filters", required = false)
    private CollectionExtraFiltersConfig collectionExtraFiltersConfig;

    @Element(name = "expandable-objects", required = true)
    private ExpandableObjectsConfig expandableObjectsConfig;

    @Element(name = "default-sort-criteria", required = false)
    private DefaultSortCriteriaConfig defaultSortCriteriaConfig;

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

    public ExpandableObjectsConfig getExpandableObjectsConfig() {
        return expandableObjectsConfig;
    }

    public void setExpandableObjectsConfig(ExpandableObjectsConfig expandableObjectsConfig) {
        this.expandableObjectsConfig = expandableObjectsConfig;
    }

    public String getGroupObjectType() {
        return groupObjectType;
    }

    public void setGroupObjectType(String groupObjectType) {
        this.groupObjectType = groupObjectType;
    }

    public String getElementObjectType() {
        return elementObjectType;
    }

    public void setElementObjectType(String elementObjectType) {
        this.elementObjectType = elementObjectType;
    }


    public DefaultSortCriteriaConfig getDefaultSortCriteriaConfig() {
        return defaultSortCriteriaConfig;
    }

    public void setDefaultSortCriteriaConfig(DefaultSortCriteriaConfig defaultSortCriteriaConfig) {
        this.defaultSortCriteriaConfig = defaultSortCriteriaConfig;
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

        if (defaultSortCriteriaConfig != null ? !defaultSortCriteriaConfig.equals(that.defaultSortCriteriaConfig) : that.
                defaultSortCriteriaConfig != null) {

            return false;
        }
        if (collectionExtraFiltersConfig != null ? !collectionExtraFiltersConfig.equals(that.collectionExtraFiltersConfig) : that.collectionExtraFiltersConfig != null) {
            return false;
        }
        if (expandableObjectsConfig != null ? !expandableObjectsConfig.equals(that.expandableObjectsConfig) : that.expandableObjectsConfig != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (groupObjectType != null ? !groupObjectType.equals(that.groupObjectType) : that.groupObjectType != null) {
            return false;
        }
        if (elementObjectType != null ? !elementObjectType.equals(that.elementObjectType) : that.elementObjectType != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = collectionExtraFiltersConfig != null ? collectionExtraFiltersConfig.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (groupObjectType != null ? groupObjectType.hashCode() : 0);
        result = 31 * result + (defaultSortCriteriaConfig != null ? defaultSortCriteriaConfig.hashCode() : 0);
        result = 31 * result + (elementObjectType != null ? elementObjectType.hashCode() : 0);
        result = 31 * result + (expandableObjectsConfig != null ? expandableObjectsConfig.hashCode() : 0);
        return result;
    }
}
