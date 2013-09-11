package ru.intertrust.cm.core.config.model.gui.navigation;

import java.util.ArrayList;
import java.util.List;
import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Author: Yaroslav Bondarchuk Date: 04.09.13 Time: 16:01
 */
@SuppressWarnings("serial")
@Root(name = "domain-object-surfer")
public class DomainObjectSurferConfig extends PluginConfigParent {
    @Attribute(name = "collection")
    private String collection;

    @Attribute(name = "collection-view")
    private String collectionView;

    @Attribute(name = "use-default")
    private boolean isUseDefault;

    @ElementList(inline = true)
    private List<SortCriterionConfig> sortCriterionConfigList = new ArrayList<SortCriterionConfig>();

    public String getCollection() {
        return collection;
    }

    public void setCollection(String collection) {
        this.collection = collection;
    }

    public List<SortCriterionConfig> getSortCriterionConfigList() {
        return sortCriterionConfigList;
    }

    public void setSortCriterionConfigList(List<SortCriterionConfig> sortCriterionConfigList) {
        this.sortCriterionConfigList = sortCriterionConfigList;
    }

    public String getCollectionView() {
        return collectionView;
    }

    public void setCollectionView(String collectionView) {
        this.collectionView = collectionView;
    }

    public boolean isUseDefault() {
        return isUseDefault;
    }

    public void setUseDefault(boolean useDefault) {
        isUseDefault = useDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DomainObjectSurferConfig that = (DomainObjectSurferConfig) o;

        if (sortCriterionConfigList != null ? !sortCriterionConfigList.equals(that.getSortCriterionConfigList()) : that.getSortCriterionConfigList() != null) {
            return false;
        }

        if (collection != null ? !collection.equals(that.getCollection()) : that.getCollection() != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = sortCriterionConfigList != null ? sortCriterionConfigList.hashCode() : 0;
        result = 23 * result + collection != null ? collection.hashCode() : 0;
        return result;
    }
}

