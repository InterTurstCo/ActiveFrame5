package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.config.model.gui.navigation.PluginConfig;

public class NavigationTreeItemSelectedEvent extends GwtEvent<NavigationTreeItemSelectedEventHandler> {

    public static Type<NavigationTreeItemSelectedEventHandler> TYPE = new Type<NavigationTreeItemSelectedEventHandler>();
    private PluginConfig pluginConfig;

    public NavigationTreeItemSelectedEvent(PluginConfig pluginConfig) {
        this.pluginConfig = pluginConfig;
    }

    @Override
    public Type<NavigationTreeItemSelectedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(NavigationTreeItemSelectedEventHandler handler) {
        handler.onNavigationTreeItemSelected(this);
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }
}
