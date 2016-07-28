package ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.CreatedObjectsConfig;
import ru.intertrust.cm.core.config.gui.form.widget.linkediting.LinkedFormMappingConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 26.07.2016
 * Time: 9:35
 * To change this template use File | Settings | File and Code Templates.
 */
@Root(name = "hierarchy-group")
public class HierarchyGroupConfig implements Dto {

    @Attribute(name = "gid", required = true)
    private String gid;

    @Attribute(name = "name", required = true)
    private String name;

    @Attribute(name = "parent-collection-id", required = false)
    private String parentCollectionId;

    @ElementList(entry = "hierarchy-group", required = false, inline=true)
    private List<HierarchyGroupConfig> hierarchyGroupConfigs = new ArrayList<>();

    @ElementList(entry = "hierarchy-collection", required = false, inline=true)
    private List<HierarchyCollectionConfig> hierarchyCollectionConfigs = new ArrayList<>();


    @Element(name = "created-objects",required = false)
    private CreatedObjectsConfig createdObjectsConfig;

    @Element(name = "linked-form-mapping",required = false)
    private LinkedFormMappingConfig linkedFormMappingConfig;

    public HierarchyGroupConfig(){}

    public String getGid() {
        return gid;
    }

    public void setGid(String gid) {
        this.gid = gid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CreatedObjectsConfig getCreatedObjectsConfig() {
        return createdObjectsConfig;
    }

    public void setCreatedObjectsConfig(CreatedObjectsConfig createdObjectsConfig) {
        this.createdObjectsConfig = createdObjectsConfig;
    }

    public LinkedFormMappingConfig getLinkedFormMappingConfig() {
        return linkedFormMappingConfig;
    }

    public void setLinkedFormMappingConfig(LinkedFormMappingConfig linkedFormMappingConfig) {
        this.linkedFormMappingConfig = linkedFormMappingConfig;
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

    public String getParentCollectionId() {
        return parentCollectionId;
    }

    public void setParentCollectionId(String parentCollectionId) {
        this.parentCollectionId = parentCollectionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HierarchyGroupConfig that = (HierarchyGroupConfig) o;
        if (gid != null
                ? !gid.equals(that.gid)
                : that.gid != null) {
            return false;
        }
        if (name != null
                ? !name.equals(that.name)
                : that.name != null) {
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
        if (parentCollectionId != null
                ? !parentCollectionId.equals(that.parentCollectionId)
                : that.parentCollectionId != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = gid != null ? gid.hashCode() : 31;
        result = 31 * result + (name != null ? name.hashCode() : 31);
        result = 31 * result + (hierarchyGroupConfigs != null ? hierarchyGroupConfigs.hashCode() : 31);
        result = 31 * result + (hierarchyCollectionConfigs != null ? hierarchyCollectionConfigs.hashCode() : 31);
        result = 31 * result + (createdObjectsConfig != null ? createdObjectsConfig.hashCode() : 31);
        result = 31 * result + (linkedFormMappingConfig != null ? linkedFormMappingConfig.hashCode() : 31);
        result = 31 * result + (parentCollectionId != null ? parentCollectionId.hashCode() : 31);
        return result;
    }
}
