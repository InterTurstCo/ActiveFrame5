package ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 18.12.2014
 *         Time: 8:01
 */
public class HierarchyBrowserChangeSelectionEvent extends GwtEvent<HierarchyBrowserChangeSelectionEventHandler> {

    public static GwtEvent.Type<HierarchyBrowserChangeSelectionEventHandler> TYPE = new GwtEvent.Type<HierarchyBrowserChangeSelectionEventHandler>();
    private HierarchyBrowserItem item;
    private boolean handleOnlyNode;
    private boolean commonSingleChoice;

    public HierarchyBrowserChangeSelectionEvent(HierarchyBrowserItem item, boolean commonSingleChoice) {
        this.item = item;
        this.commonSingleChoice = commonSingleChoice;
    }

    @Override
    public GwtEvent.Type<HierarchyBrowserChangeSelectionEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserChangeSelectionEventHandler handler) {
        handler.onChangeSelectionEvent(this);
    }

    public HierarchyBrowserItem getItem() {
        return item;
    }
    @Deprecated //returns false always
    public boolean isHandleOnlyNode() {
        return handleOnlyNode;
    }

    public boolean isCommonSingleChoice() {
        return commonSingleChoice;
    }
}
