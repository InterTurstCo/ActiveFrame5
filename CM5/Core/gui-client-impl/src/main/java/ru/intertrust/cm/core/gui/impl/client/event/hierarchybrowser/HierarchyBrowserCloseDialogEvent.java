package ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class HierarchyBrowserCloseDialogEvent extends GwtEvent<HierarchyBrowserCloseDialogEventHandler> {

    public static Type<HierarchyBrowserCloseDialogEventHandler> TYPE = new Type<HierarchyBrowserCloseDialogEventHandler>();

    @Override
    public Type<HierarchyBrowserCloseDialogEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HierarchyBrowserCloseDialogEventHandler handler) {
        handler.onHierarchyBrowserCloseDialogEvent(this);
    }
}