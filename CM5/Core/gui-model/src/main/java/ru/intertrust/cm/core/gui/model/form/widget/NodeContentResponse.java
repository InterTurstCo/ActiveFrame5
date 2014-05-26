package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.List;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.12.13
 *         Time: 13:15
 */
public class NodeContentResponse implements Dto {

    private List<HierarchyBrowserItem> nodeContent;
    private String parentCollectionName;
    private Map<String, String> domainObjectTypesAndTitles;
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

    public Map<String, String> getDomainObjectTypesAndTitles() {
        return domainObjectTypesAndTitles;
    }

    public void setDomainObjectTypesAndTitles(Map<String, String> domainObjectTypesAndTitles) {
        this.domainObjectTypesAndTitles = domainObjectTypesAndTitles;
    }
}
