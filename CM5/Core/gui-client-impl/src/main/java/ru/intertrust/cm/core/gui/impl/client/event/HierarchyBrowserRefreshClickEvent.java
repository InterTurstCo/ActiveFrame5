package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserRefreshClickEvent extends GwtEvent<HierarchyBrowserRefreshClickEventHandler> {

    public static Type<HierarchyBrowserRefreshClickEventHandler> TYPE = new Type<HierarchyBrowserRefreshClickEventHandler>();
    private String collectionName;
    private Id parentId;

    public HierarchyBrowserRefreshClickEvent(String collectionName, Id parentId) {
        this.collectionName = collectionName;
        this.parentId = parentId;

    }

    @Override
    public Type<HierarchyBrowserRefreshClickEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserRefreshClickEventHandler handler) {
        handler.onHierarchyBrowserRefreshClick(this);
    }

    public String getCollectionName() {
        return collectionName;
    }

    public Id getParentId() {
        return parentId;
    }
}