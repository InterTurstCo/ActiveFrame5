package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.config.gui.navigation.PluginConfig;

public class NavigationTreeItemSelectedEvent extends GwtEvent<NavigationTreeItemSelectedEventHandler> {

    public static Type<NavigationTreeItemSelectedEventHandler> TYPE = new Type<NavigationTreeItemSelectedEventHandler>();
    private final PluginConfig pluginConfig;
    private final String linkName;

    public NavigationTreeItemSelectedEvent(final PluginConfig pluginConfig, final String linkName) {
        this.pluginConfig = pluginConfig;
        this.linkName = linkName;
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

    public String getLinkName() {
        return linkName;
    }
}
