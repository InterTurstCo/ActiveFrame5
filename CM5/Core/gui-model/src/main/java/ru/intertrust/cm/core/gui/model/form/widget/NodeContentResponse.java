package ru.intertrust.cm.core.gui.model.form.widget;

import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 23.12.13
 *         Time: 13:15
 */
public class NodeContentResponse implements Dto {
    private NodeMetadata nodeMetadata;
    private ArrayList<HierarchyBrowserItem> nodeContent;

    private boolean selective = true;
    public ArrayList<HierarchyBrowserItem> getNodeContent() {
        return nodeContent;
    }

    public void setNodeContent(ArrayList<HierarchyBrowserItem> nodeContent) {
        this.nodeContent = nodeContent;
    }

    public NodeMetadata getNodeMetadata() {
        return nodeMetadata;
    }

    public void setNodeMetadata(NodeMetadata nodeMetadata) {
        this.nodeMetadata = nodeMetadata;
    }

    public boolean isSelective() {
        return selective;
    }

    public void setSelective(boolean selective) {
        this.selective = selective;
    }
}
