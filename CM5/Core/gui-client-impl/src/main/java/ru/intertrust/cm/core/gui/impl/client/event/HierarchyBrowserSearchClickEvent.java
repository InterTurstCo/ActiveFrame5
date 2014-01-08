package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserSearchClickEvent extends GwtEvent<HierarchyBrowserSearchClickEventHandler> {

    public static Type<HierarchyBrowserSearchClickEventHandler> TYPE = new Type<HierarchyBrowserSearchClickEventHandler>();
    private String collectionName;
    private Id parentId;
    private String inputText;
    public HierarchyBrowserSearchClickEvent(String collectionName, Id parentId, String inputText) {
        this.collectionName = collectionName;
        this.parentId = parentId;
        this.inputText = inputText;
    }

    @Override
    public Type<HierarchyBrowserSearchClickEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserSearchClickEventHandler handler) {
        handler.onHierarchyBrowserSearchClick(this);
    }

    public String getCollectionName() {
        return collectionName;
    }

    public Id getParentId() {
        return parentId;
    }

    public String getInputText() {
        return inputText;
    }
}