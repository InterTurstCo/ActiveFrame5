package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserAddItemClickEvent extends GwtEvent<HierarchyBrowserAddItemClickEventHandler> {

    public static Type<HierarchyBrowserAddItemClickEventHandler> TYPE = new Type<HierarchyBrowserAddItemClickEventHandler>();
    private Id parentId;
    private Map.Entry<String, String> entry;
    private String parentCollectionName;
    public HierarchyBrowserAddItemClickEvent(Id parentId, String parentCollectionName, Map.Entry<String, String> entry) {
        this.parentId = parentId;
        this.entry = entry;
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

    public Map.Entry<String, String> getEntry() {
        return entry;
    }

    public String getParentCollectionName() {
        return parentCollectionName;
    }
}
