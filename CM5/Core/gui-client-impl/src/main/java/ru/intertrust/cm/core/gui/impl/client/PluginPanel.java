package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEvent;
import ru.intertrust.cm.core.gui.impl.client.event.PluginViewCreatedEventHandler;

/**
 * Панель плагинов. Имеет возможность отображать плагины (как самостоятельные, так и дочерние), поддерживая порядок их
 * открытия.
 *
 * @author Denis Mitavskiy
 *         Date: 20.08.13
 *         Time: 16:53
 */
public class PluginPanel implements IsWidget, PluginViewCreatedEventHandler {
    private SimplePanel impl = new SimplePanel();
    private EventBus eventBus = GWT.create(SimpleEventBus.class);

    /**
     * <p>
     * Открывает плагин, замещая предыдущий, если он был открыт. Операция необратима, восстановить предыдущий плагин или
     * любого его открытия невозможно.
     * </p>
     *
     * @param plugin плагин, который нужно открыть в панели
     */
    public void open(Plugin plugin) {
        eventBus.addHandlerToSource(PluginViewCreatedEvent.TYPE, plugin, this);
        plugin.setEventBus(eventBus);
        plugin.setOwner(this);
        plugin.setUp();
    }

    /**
     * <p>
     * Открывает дочерний плагин, замещая предыдущий, если он был открыт. Восстановить предыдущий плагин, вызвав метод
     * {@link #closeCurrentPlugin()}.
     * </p>
     *
     * @param plugin плагин, который нужно открыть в панели
     */
    public void openChild(Plugin plugin) {

    }

    /**
     * Закрывает текущий плагин. Если у него есть родитель, то он будет показан по закрытию текущего.
     */
    public void closeCurrentPlugin() {

    }

    /**
     * Закрывает все плагины. Операция необратима.
     */
    public void closeAllPlugins() {

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

    public void setSize(String width, String height) {
        impl.setSize(width, height);
    }
}
