package ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 26.12.13
 *         Time: 11:15
 */
public class HierarchyBrowserScrollEvent extends GwtEvent<HierarchyBrowserScrollEventHandler> {

    public static Type<HierarchyBrowserScrollEventHandler> TYPE = new Type<HierarchyBrowserScrollEventHandler>();
    private Id parentId;
    private String parentCollectionName;
    private String inputText;
    private int factor;
    public HierarchyBrowserScrollEvent(Id parentId, String parentCollectionName, int factor, String inputText){
        this.parentId = parentId;
        this.parentCollectionName = parentCollectionName;
        this.factor = factor;
        this.inputText = inputText;
    }

    @Override
    public Type<HierarchyBrowserScrollEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserScrollEventHandler handler) {
        handler.onHierarchyBrowserScroll(this);
    }

    public Id getParentId() {
        return parentId;
    }

    public String getParentCollectionName() {
        return parentCollectionName;
    }

    public int getFactor() {
        return factor;
    }

    public String getInputText() {
        return inputText;
    }
}