package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.gui.impl.client.Plugin;

/**
 * @author Sergey.Okolot
 *         Created on 24.01.14 10:09.
 */
public class CentralPluginChildOpeningRequestedEvent extends GwtEvent<CentralPluginChildOpeningRequestedHandler> {

    public static final Type<CentralPluginChildOpeningRequestedHandler> TYPE =
            new Type<CentralPluginChildOpeningRequestedHandler>();

    private final Plugin plugin;

    public CentralPluginChildOpeningRequestedEvent(final Plugin plugin) {
        this.plugin = plugin;
    }

    public Plugin getOpeningChildPlugin() {
        return plugin;
    }


    @Override
    public Type<CentralPluginChildOpeningRequestedHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CentralPluginChildOpeningRequestedHandler handler) {
        handler.openChildPlugin(this);
    }
}
