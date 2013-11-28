package ru.intertrust.cm.core.gui.impl.client;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Панель плагинов. Имеет возможность отображать плагины (как самостоятельные, так и дочерние), поддерживая порядок их
 * открытия.
 *
 * @author Denis Mitavskiy
 *         Date: 20.08.13
 *         Time: 16:53
 */
public class PluginPanel implements IsWidget {

    private int panelWidth;
    private int panelHeight;
    private SimplePanel impl = new SimplePanel();
    private Plugin currentPlugin;

    public PluginPanel() {

    }

    public int getPanelWidth() {
        return panelWidth;
    }

    public void setPanelWidth(int panelWidth) {
        this.panelWidth = panelWidth;
    }

    public int getPanelHeight() {
        return panelHeight;
    }

    public void setPanelHeight(int panelHeight) {
        this.panelHeight = panelHeight;
    }

    /**
     * <p>
     * Открывает плагин, замещая предыдущий, если он был открыт. Операция необратима, восстановить предыдущий плагин или
     * любого его открытия невозможно.
     * </p>
     *
     * @param plugin плагин, который нужно открыть в панели
     */
    public void open(Plugin plugin) {
        open(plugin, null);
    }

    public void open(Plugin plugin, Dto initParams) {

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
        if (currentPlugin == null) {
            return;
        }
        currentPlugin.clearHandlers();
        impl.remove(asWidget());
    }

    /**
     * Закрывает все плагины. Операция необратима.
     */
    public void closeAllPlugins() {

    }

    public void beforePluginOpening() {
        // noop
    }

    public void onPluginViewCreated(Plugin plugin) {
        this.currentPlugin = plugin;
        beforePluginOpening();
        impl.setWidget(this.currentPlugin.getView());
        impl.setSize("100%", "100%");

    }

    @Override
    public Widget asWidget() {
        return impl;
    }

    public void setSize(String width, String height) {
        impl.setSize(width, height);
    }
}
