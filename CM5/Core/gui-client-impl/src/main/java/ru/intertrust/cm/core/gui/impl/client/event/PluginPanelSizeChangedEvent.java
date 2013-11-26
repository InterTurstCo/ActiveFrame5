package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

public class PluginPanelSizeChangedEvent extends GwtEvent<PluginPanelSizeChangedEventHandler> {
    public static Type<PluginPanelSizeChangedEventHandler> TYPE = new Type<PluginPanelSizeChangedEventHandler>();

    @Override
    public Type<PluginPanelSizeChangedEventHandler> getAssociatedType() {
        return TYPE;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void dispatch(PluginPanelSizeChangedEventHandler handler) {
       handler.updateSizes();
    }
}
