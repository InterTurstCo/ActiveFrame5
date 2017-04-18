package ru.intertrust.cm.core.config.gui.navigation.listplugin;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;

/**
 * Created by Ravil on 10.04.2017.
 */
@Root(name = "list-collection")
public class ListCollectionConfig implements Dto  {
    @Attribute(name = "selection-mark", required = false)
    private Boolean selectionMark;

    @Element(name = "list-collection-ref", required = true)
    private CollectionRefConfig collectionRefConfig;

    @Element(name = "list-collection-view",required = true)
    protected ListCollectionViewConfig listCollectionViewConfig;

    public ListCollectionConfig(){}

    public Boolean getSelectionMark() {
        return selectionMark;
    }

    public void setSelectionMark(Boolean selectionMark) {
        this.selectionMark = selectionMark;
    }

    public CollectionRefConfig getCollectionRefConfig() {
        return collectionRefConfig;
    }

    public void setCollectionRefConfig(CollectionRefConfig collectionRefConfig) {
        this.collectionRefConfig = collectionRefConfig;
    }

    public ListCollectionViewConfig getListCollectionViewConfig() {
        return listCollectionViewConfig;
    }

    public void setListCollectionViewConfig(ListCollectionViewConfig listCollectionViewConfig) {
        this.listCollectionViewConfig = listCollectionViewConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ListCollectionConfig that = (ListCollectionConfig) o;
        if (selectionMark != null
                ? !selectionMark.equals(that.selectionMark)
                : that.selectionMark != null) {
            return false;
        }

        if (listCollectionViewConfig != null
                ? !listCollectionViewConfig.equals(that.listCollectionViewConfig)
                : that.listCollectionViewConfig != null) {
            return false;
        }
        if (collectionRefConfig != null
                ? !collectionRefConfig.equals(that.collectionRefConfig)
                : that.collectionRefConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = selectionMark != null ? selectionMark.hashCode() : 31;
        result = 31 * result + (listCollectionViewConfig != null ? listCollectionViewConfig.hashCode() : 31);
        result = 31 * result + (collectionRefConfig != null ? collectionRefConfig.hashCode() : 31);
        return result;
    }
}
