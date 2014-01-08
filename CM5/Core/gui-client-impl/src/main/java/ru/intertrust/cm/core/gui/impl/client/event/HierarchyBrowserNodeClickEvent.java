package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserNodeClickEvent extends GwtEvent<HierarchyBrowserNodeClickEventHandler> {

    public static Type<HierarchyBrowserNodeClickEventHandler> TYPE = new Type<HierarchyBrowserNodeClickEventHandler>();
    private String collectionName;
    private Id parentId;

    public HierarchyBrowserNodeClickEvent(String collectionName, Id parentId) {
        this.collectionName = collectionName;
        this.parentId = parentId;

    }

    @Override
    public Type<HierarchyBrowserNodeClickEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserNodeClickEventHandler handler) {
        handler.onHierarchyBrowserNodeClick(this);
    }

    public String getCollectionName() {
        return collectionName;
    }

    public Id getParentId() {
        return parentId;
    }
}

