package ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserRefreshClickEvent extends GwtEvent<HierarchyBrowserRefreshClickEventHandler> {

    public static Type<HierarchyBrowserRefreshClickEventHandler> TYPE = new Type<HierarchyBrowserRefreshClickEventHandler>();
    private Id parentId;
    private String parentCollectionName;
    private int recursionDeepness;

    public HierarchyBrowserRefreshClickEvent(Id parentId, String parentCollectionName, int recursionDeepness) {
        this.parentId = parentId;
        this.parentCollectionName = parentCollectionName;
        this.recursionDeepness = recursionDeepness;

    }

    @Override
    public Type<HierarchyBrowserRefreshClickEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserRefreshClickEventHandler handler) {
        handler.onHierarchyBrowserRefreshClick(this);
    }

    public Id getParentId() {
        return parentId;
    }

    public String getParentCollectionName() {
        return parentCollectionName;
    }

    public int getRecursionDeepness() {
        return recursionDeepness;
    }
}