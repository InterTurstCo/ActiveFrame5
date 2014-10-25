package ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserAddItemClickEvent extends GwtEvent<HierarchyBrowserAddItemClickEventHandler> {

    public static Type<HierarchyBrowserAddItemClickEventHandler> TYPE = new Type<HierarchyBrowserAddItemClickEventHandler>();
    private Id parentId;
    private NodeCollectionDefConfig nodeConfig;
    private String parentCollectionName;
    private String domainObjectType;
    public HierarchyBrowserAddItemClickEvent(Id parentId, String parentCollectionName, String domainObjectType,
                                             NodeCollectionDefConfig nodeConfig) {
        this.parentId = parentId;
        this.nodeConfig = nodeConfig;
        this.parentCollectionName = parentCollectionName;
        this.domainObjectType = domainObjectType;

    }

    @Override
    public Type<HierarchyBrowserAddItemClickEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserAddItemClickEventHandler handler) {
        handler.onHierarchyBrowserAddItemClick(this);
    }

    public Id getParentId() {
        return parentId;
    }

    public NodeCollectionDefConfig getNodeConfig() {
        return nodeConfig;
    }

    public String getParentCollectionName() {
        return parentCollectionName;
    }

    public String getDomainObjectType() {
        return domainObjectType;
    }
}
