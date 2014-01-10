package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;

/**
 * User: IPetrov
 * Date: 08.01.14
 * Time: 13:09
 * Визуализация формы расширенного поиска в составе плагина РП
 */
public class ExtendedSearchFormPluginView extends PluginView {

    @Override
    protected IsWidget getViewWidget() {
        final VerticalPanel extendedSearchForm = new VerticalPanel();
        return extendedSearchForm;
    }

    public ExtendedSearchFormPluginView(Plugin plugin) {
        super(plugin);
    }
}
