package ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserSearchClickEvent extends GwtEvent<HierarchyBrowserSearchClickEventHandler> {

    public static Type<HierarchyBrowserSearchClickEventHandler> TYPE = new Type<HierarchyBrowserSearchClickEventHandler>();
    private Id parentId;
    private String parentCollectionName;
    private String inputText;
    public HierarchyBrowserSearchClickEvent(Id parentId, String parentCollectionName, String inputText) {
        this.parentId = parentId;
        this.parentCollectionName = parentCollectionName;
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

    public Id getParentId() {
        return parentId;
    }

    public String getParentCollectionName() {
        return parentCollectionName;
    }

    public String getInputText() {
        return inputText;
    }
}