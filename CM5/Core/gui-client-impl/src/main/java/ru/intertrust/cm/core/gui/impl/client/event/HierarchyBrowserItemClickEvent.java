package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserItemClickEvent extends GwtEvent<HierarchyBrowserItemClickEventHandler> {

    public static Type<HierarchyBrowserItemClickEventHandler> TYPE = new Type<HierarchyBrowserItemClickEventHandler>();
    private Id itemId;
    private String collectionName;

    public HierarchyBrowserItemClickEvent(Id itemId, String collectionName) {
        this.itemId = itemId;
        this.collectionName = collectionName;

    }

    @Override
    public Type<HierarchyBrowserItemClickEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserItemClickEventHandler handler) {
        handler.onHierarchyBrowserItemClick(this);
    }

    public Id getItemId() {
        return itemId;
    }

    public String getCollectionName() {
        return collectionName;
    }
}
