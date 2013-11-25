package ru.intertrust.cm.core.config.gui.collection.view;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
@Root(name = "collection-mappings")
public class CollectionMappingsConfig implements TopLevelConfig, Dto {
    @Attribute(name = "name", required = false)
    private String name;
    @ElementList(inline = true)
    private List<CollectionMappingConfig> collectionMappingConfigList = new ArrayList<CollectionMappingConfig>();


    public String getName() {
        return name != null ? name : "collection-mappings";
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<CollectionMappingConfig> getCollectionMappingConfigList() {
        return collectionMappingConfigList;
    }

    public void setCollectionMappingConfigList(List<CollectionMappingConfig> collectionMappingConfigList) {
        this.collectionMappingConfigList = collectionMappingConfigList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CollectionMappingsConfig that = (CollectionMappingsConfig) o;

        if (collectionMappingConfigList != null ? !collectionMappingConfigList.equals(that.
                collectionMappingConfigList) : that.collectionMappingConfigList != null) {
                    return false;
        }

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (collectionMappingConfigList != null ? collectionMappingConfigList.hashCode() : 0);
        return result;
    }
}
