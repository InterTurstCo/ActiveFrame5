package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserItemClickEvent extends GwtEvent<HierarchyBrowserItemClickEventHandler> {

    public static Type<HierarchyBrowserItemClickEventHandler> TYPE = new Type<HierarchyBrowserItemClickEventHandler>();
    private HierarchyBrowserItem item;

    public HierarchyBrowserItemClickEvent(HierarchyBrowserItem item) {
        this.item = item;

    }

    @Override
    public Type<HierarchyBrowserItemClickEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserItemClickEventHandler handler) {
        handler.onHierarchyBrowserItemClick(this);
    }

    public HierarchyBrowserItem getItem() {
        return item;
    }
}
