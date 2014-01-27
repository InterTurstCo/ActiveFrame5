package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;

/**
 * User: IPetrov
 * Date: 08.01.14
 * Time: 13:09
 * Плагин формы расширенного поиска в составе плагина РП
 */
@ComponentName("extended.search.form.plugin")
public class ExtendedSearchFormPlugin extends Plugin {

    private ExtendedSearchData extendedSearchData;

    public ExtendedSearchData getExtendedSearchData() {
        return extendedSearchData;
    }

    public void setExtendedSearchData(ExtendedSearchData extendedSearchData) {
        this.extendedSearchData = extendedSearchData;
    }

    @Override
    public PluginView createView() {
        FormPluginData initialData = getInitialData();
        return new ExtendedSearchFormPluginView(this, initialData.getFormDisplayData());
    }

    @Override
    public ExtendedSearchFormPlugin createNew() {
        return new ExtendedSearchFormPlugin();
    }

    public ExtendedSearchFormPlugin() {
    }
}
