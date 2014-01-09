package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.12.13
 *         Time: 13:15
 */
public class NodeContentResponse implements Dto {
    private String nodeType;
    private ArrayList<HierarchyBrowserItem> nodeContent;
    private Id parentId;
    private boolean selective = true;
    public ArrayList<HierarchyBrowserItem> getNodeContent() {
        return nodeContent;
    }

    public void setNodeContent(ArrayList<HierarchyBrowserItem> nodeContent) {
        this.nodeContent = nodeContent;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
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
}
