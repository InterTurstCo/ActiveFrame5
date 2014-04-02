package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by lvov on 02.04.14.
 */
public class HeaderNotificationRemoveItemEvent extends GwtEvent<HeaderNotificationRemoveItemEventHandler> {

    public static Type<HeaderNotificationRemoveItemEventHandler> TYPE = new Type<HeaderNotificationRemoveItemEventHandler>();
    private int pluginRowCount;


    public HeaderNotificationRemoveItemEvent(int pluginRowCount) {
        this.pluginRowCount = pluginRowCount;
    }

    public static Type<HeaderNotificationRemoveItemEventHandler> getTYPE() {
        return TYPE;
    }

    public static void setTYPE(Type<HeaderNotificationRemoveItemEventHandler> TYPE) {
        HeaderNotificationRemoveItemEvent.TYPE = TYPE;
    }

    public int getPluginRowCount() {
        return pluginRowCount;
    }

    public void setPluginRowCount(int pluginRowCount) {
        this.pluginRowCount = pluginRowCount;
    }

    @Override
    public Type<HeaderNotificationRemoveItemEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HeaderNotificationRemoveItemEventHandler handler) {
        handler.headerNotificationPopupStatus(this);


    }
}
