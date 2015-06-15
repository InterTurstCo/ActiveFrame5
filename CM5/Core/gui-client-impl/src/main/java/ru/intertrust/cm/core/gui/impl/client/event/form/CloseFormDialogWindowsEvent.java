package ru.intertrust.cm.core.gui.impl.client.event.form;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 10.06.2015
 *         Time: 8:26
 */
public class CloseFormDialogWindowsEvent extends GwtEvent<CloseFormDialogWindowEventHandler> {
    public static Type<CloseFormDialogWindowEventHandler> TYPE = new Type<CloseFormDialogWindowEventHandler>();

    @Override
    public Type<CloseFormDialogWindowEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CloseFormDialogWindowEventHandler handler) {
        handler.forceClosing();
    }

}

