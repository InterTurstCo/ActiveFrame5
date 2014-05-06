package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.12.13
 *         Time: 13:15
 */
public class NodeContentResponse implements Dto {

    private List<HierarchyBrowserItem> nodeContent;
    private String parentCollectionName;
    private List<String> domainObjectTypes;
    private Id parentId;

    private boolean selective = true;
    public List<HierarchyBrowserItem> getNodeContent() {
        return nodeContent;
    }

    public void setNodeContent(List<HierarchyBrowserItem> nodeContent) {
        this.nodeContent = nodeContent;
    }

    public Id getParentId() {
        return parentId;
    }

    public void setParentId(Id parentId) {
        this.parentId = parentId;
    }

    public boolean isSelective() {
        return selective;
    }

    public void setSelective(boolean selective) {
        this.selective = selective;
    }

    public String getParentCollectionName() {
        return parentCollectionName;
    }

    public void setParentCollectionName(String parentCollectionName) {
        this.parentCollectionName = parentCollectionName;
    }

    public List<String> getDomainObjectTypes() {
        return domainObjectTypes;
    }

    public void setDomainObjectTypes(List<String> domainObjectTypes) {
        this.domainObjectTypes = domainObjectTypes;
    }
}
