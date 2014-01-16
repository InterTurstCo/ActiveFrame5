package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.form.widget.NodeMetadata;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserAddItemClickEvent extends GwtEvent<HierarchyBrowserAddItemClickEventHandler> {

    public static Type<HierarchyBrowserAddItemClickEventHandler> TYPE = new Type<HierarchyBrowserAddItemClickEventHandler>();
    private NodeMetadata metadata;
    public HierarchyBrowserAddItemClickEvent(NodeMetadata metadata) {
        this.metadata = metadata;

    }

    @Override
    public Type<HierarchyBrowserAddItemClickEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserAddItemClickEventHandler handler) {
        handler.onHierarchyBrowserAddItemClick(this);
    }

    public NodeMetadata getMetadata() {
        return metadata;
    }
}
