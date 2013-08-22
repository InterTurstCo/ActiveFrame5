package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventHandler;

/**
 * @author Denis Mitavskiy
 *         Date: 20.08.13
 *         Time: 16:53
 */
public class PluginPanel implements IsWidget, PluginViewCreatedEventHandler {
    private SimplePanel impl = new SimplePanel();
    private EventBus eventBus = GWT.create(SimpleEventBus.class);

    public Panel getPanel() {
        impl.setSize("100%", "100%");
        return impl;
    }

    public void open(Plugin plugin) {
        eventBus.addHandlerToSource(PluginViewCreatedEvent.TYPE, plugin, this);
        plugin.setEventBus(eventBus);
        plugin.setUp();

    }

    @Override
    public void onPluginViewCreated(PluginViewCreatedEvent event) {
        impl.setWidget(event.getPlugin().getView());
    }

    @Override
    public Widget asWidget() {
        impl.setSize("100%", "100%");
        return impl;
    }
}
