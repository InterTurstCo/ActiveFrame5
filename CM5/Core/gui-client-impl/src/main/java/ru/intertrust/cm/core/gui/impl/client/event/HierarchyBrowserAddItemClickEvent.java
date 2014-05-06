package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserAddItemClickEvent extends GwtEvent<HierarchyBrowserAddItemClickEventHandler> {

    public static Type<HierarchyBrowserAddItemClickEventHandler> TYPE = new Type<HierarchyBrowserAddItemClickEventHandler>();
    private Id parentId;
    private String domainObjectType;
    private String parentCollectionName;
    public HierarchyBrowserAddItemClickEvent(Id parentId, String parentCollectionName, String domainObjectType) {
        this.parentId = parentId;
        this.domainObjectType = domainObjectType;
        this.parentCollectionName = parentCollectionName;

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

    public String getDomainObjectType() {
        return domainObjectType;
    }

    public String getParentCollectionName() {
        return parentCollectionName;
    }
}
