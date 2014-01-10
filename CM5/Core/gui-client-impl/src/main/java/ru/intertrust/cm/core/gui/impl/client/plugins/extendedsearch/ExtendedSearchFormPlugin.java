package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * User: IPetrov
 * Date: 08.01.14
 * Time: 13:09
 * Плагин формы расширенного поиска в составе плагина РП
 */
@ComponentName("extended.search.form.plugin")
public class ExtendedSearchFormPlugin extends Plugin {

    @Override
    public PluginView createView() {
        return new ExtendedSearchFormPluginView(this);
    }

    @Override
    public ExtendedSearchFormPlugin createNew() {
        return new ExtendedSearchFormPlugin();
    }

    public ExtendedSearchFormPlugin() {
    }
}
