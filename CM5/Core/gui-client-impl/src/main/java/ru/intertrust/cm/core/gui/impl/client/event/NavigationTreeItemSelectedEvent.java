package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class NavigationTreeItemSelectedEvent extends GwtEvent<NavigationTreeItemSelectedEventHandler> {

    public static Type<NavigationTreeItemSelectedEventHandler> TYPE = new Type<NavigationTreeItemSelectedEventHandler>();

    @Override
    public Type<NavigationTreeItemSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(NavigationTreeItemSelectedEventHandler handler) {
        handler.onNavigationTreeItemSelected(this);
    }
}
