package ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;
import ru.intertrust.cm.core.config.gui.navigation.CollectionRefConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 26.07.2016
 * Time: 10:01
 * To change this template use File | Settings | File and Code Templates.
 */
@Root(name = "hierarchy-collection")
public class HierarchyCollectionConfig implements Dto {
    @Attribute(name = "cid", required = true)
    private String cid;

    @Attribute(name = "up-link-field", required = false)
    private String upLinkField;

    @Attribute(name = "down-link-field", required = false)
    private String downLinkField;

    @Element(name = "hierarchy-collection-view",required = true)
    protected HierarchyCollectionViewConfig hierarchyCollectionViewConfig;

    @Element(name = "collection-ref", required = true)
    private CollectionRefConfig collectionRefConfig;

    @ElementList(entry = "hierarchy-group", required = false, inline=true)
    private List<HierarchyGroupConfig> hierarchyGroupConfigs = new ArrayList<>();

    @ElementList(entry = "hierarchy-collection", required = false, inline=true)
    private List<HierarchyCollectionConfig> hierarchyCollectionConfigs = new ArrayList<>();

    @Element(name = "created-objects",required = false)
    private CreatedObjectsConfig createdObjectsConfig;

    @Element(name = "linked-form-mapping",required = false)
    private LinkedFormMappingConfig linkedFormMappingConfig;

    public HierarchyCollectionConfig(){}

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getUpLinkField() {
        return upLinkField;
    }

    public void setUpLinkField(String upLinkField) {
        this.upLinkField = upLinkField;
    }

    public String getDownLinkField() {
        return downLinkField;
    }

    public void setDownLinkField(String downLinkField) {
        this.downLinkField = downLinkField;
    }

    public HierarchyCollectionViewConfig getHierarchyCollectionViewConfig() {
        return hierarchyCollectionViewConfig;
    }

    public void setHierarchyCollectionViewConfig(HierarchyCollectionViewConfig hierarchyCollectionViewConfig) {
        this.hierarchyCollectionViewConfig = hierarchyCollectionViewConfig;
    }

    public CollectionRefConfig getCollectionRefConfig() {
        return collectionRefConfig;
    }

    public void setCollectionRefConfig(CollectionRefConfig collectionRefConfig) {
        this.collectionRefConfig = collectionRefConfig;
    }

    public List<HierarchyGroupConfig> getHierarchyGroupConfigs() {
        return hierarchyGroupConfigs;
    }

    public void setHierarchyGroupConfigs(List<HierarchyGroupConfig> hierarchyGroupConfigs) {
        this.hierarchyGroupConfigs = hierarchyGroupConfigs;
    }

    public List<HierarchyCollectionConfig> getHierarchyCollectionConfigs() {
        return hierarchyCollectionConfigs;
    }

    public void setHierarchyCollectionConfigs(List<HierarchyCollectionConfig> hierarchyCollectionConfigs) {
        this.hierarchyCollectionConfigs = hierarchyCollectionConfigs;
    }

    public LinkedFormMappingConfig getLinkedFormMappingConfig() {
        return linkedFormMappingConfig;
    }

    public void setLinkedFormMappingConfig(LinkedFormMappingConfig linkedFormMappingConfig) {
        this.linkedFormMappingConfig = linkedFormMappingConfig;
    }

    public CreatedObjectsConfig getCreatedObjectsConfig() {
        return createdObjectsConfig;
    }

    public void setCreatedObjectsConfig(CreatedObjectsConfig createdObjectsConfig) {
        this.createdObjectsConfig = createdObjectsConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HierarchyCollectionConfig that = (HierarchyCollectionConfig) o;
        if (cid != null
                ? !cid.equals(that.cid)
                : that.cid != null) {
            return false;
        }
        if (upLinkField != null
                ? !upLinkField.equals(that.upLinkField)
                : that.upLinkField != null) {
            return false;
        }
        if (downLinkField != null
                ? !downLinkField.equals(that.downLinkField)
                : that.downLinkField != null) {
            return false;
        }
        if (hierarchyCollectionViewConfig != null
                ? !hierarchyCollectionViewConfig.equals(that.hierarchyCollectionViewConfig)
                : that.hierarchyCollectionViewConfig != null) {
            return false;
        }
        if (collectionRefConfig != null
                ? !collectionRefConfig.equals(that.collectionRefConfig)
                : that.collectionRefConfig != null) {
            return false;
        }
        if (hierarchyGroupConfigs != null
                ? !hierarchyGroupConfigs.equals(that.hierarchyGroupConfigs)
                : that.hierarchyGroupConfigs != null) {
            return false;
        }
        if (hierarchyCollectionConfigs != null
                ? !hierarchyCollectionConfigs.equals(that.hierarchyCollectionConfigs)
                : that.hierarchyCollectionConfigs != null) {
            return false;
        }
        if (createdObjectsConfig != null
                ? !createdObjectsConfig.equals(that.createdObjectsConfig)
                : that.createdObjectsConfig != null) {
            return false;
        }
        if (linkedFormMappingConfig != null
                ? !linkedFormMappingConfig.equals(that.linkedFormMappingConfig)
                : that.linkedFormMappingConfig != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = cid != null ? cid.hashCode() : 31;
        result = 31 * result + (upLinkField != null ? upLinkField.hashCode() : 31);
        result = 31 * result + (downLinkField != null ? downLinkField.hashCode() : 31);
        result = 31 * result + (hierarchyCollectionViewConfig != null ? hierarchyCollectionViewConfig.hashCode() : 31);
        result = 31 * result + (collectionRefConfig != null ? collectionRefConfig.hashCode() : 31);
        result = 31 * result + (hierarchyGroupConfigs != null ? hierarchyGroupConfigs.hashCode() : 31);
        result = 31 * result + (hierarchyCollectionConfigs != null ? hierarchyCollectionConfigs.hashCode() : 31);
        result = 31 * result + (createdObjectsConfig != null ? createdObjectsConfig.hashCode() : 31);
        result = 31 * result + (linkedFormMappingConfig != null ? linkedFormMappingConfig.hashCode() : 31);
        return result;
    }
}
