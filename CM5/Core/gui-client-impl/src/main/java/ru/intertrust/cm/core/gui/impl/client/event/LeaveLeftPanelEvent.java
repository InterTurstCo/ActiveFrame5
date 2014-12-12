package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Lesia Puhova
 *         Date: 17.10.14
 *         Time: 16:31
 */
public class LeaveLeftPanelEvent extends GwtEvent<LeaveLeftPanelEventHandler> {

    public static final Type<LeaveLeftPanelEventHandler> TYPE = new Type<>();

    @Override
    public Type<LeaveLeftPanelEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(LeaveLeftPanelEventHandler handler) {
        handler.onLeavingLeftPanel(this);
    }
}
