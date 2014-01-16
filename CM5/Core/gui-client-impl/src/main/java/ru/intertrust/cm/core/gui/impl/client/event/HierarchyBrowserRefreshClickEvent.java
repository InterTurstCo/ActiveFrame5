package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.form.widget.NodeMetadata;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserRefreshClickEvent extends GwtEvent<HierarchyBrowserRefreshClickEventHandler> {

    public static Type<HierarchyBrowserRefreshClickEventHandler> TYPE = new Type<HierarchyBrowserRefreshClickEventHandler>();
    private NodeMetadata nodeMetadata;

    public HierarchyBrowserRefreshClickEvent(NodeMetadata nodeMetadata) {
        this.nodeMetadata = nodeMetadata;

    }

    @Override
    public Type<HierarchyBrowserRefreshClickEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserRefreshClickEventHandler handler) {
        handler.onHierarchyBrowserRefreshClick(this);
    }

    public NodeMetadata getNodeMetadata() {
        return nodeMetadata;
    }
}