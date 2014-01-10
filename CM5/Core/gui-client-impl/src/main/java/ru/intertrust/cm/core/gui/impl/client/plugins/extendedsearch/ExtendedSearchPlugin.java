package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchPluginData;

/*
* @author IPetrov
*    Date: 08.01.14
*    Time: 13:45
*    Плагин расширенного поиска
*/

@ComponentName("extended.search.plugin")
public class ExtendedSearchPlugin extends Plugin {

    private ExtendedSearchFormPlugin extendedSearchFormPlugin;
    private PluginPanel extendedSearchPluginPanel;
    private ExtendedSearchPluginData extendedSearchPluginData;

    public ExtendedSearchPluginData getExtendedSearchPluginData() {
        return extendedSearchPluginData;
    }

    public PluginPanel getExtendedSearchPluginPanel() {
        return extendedSearchPluginPanel;
    }

    public void setExtendedSearchPluginPanel(PluginPanel extendedSearchPluginPanel) {
        this.extendedSearchPluginPanel = extendedSearchPluginPanel;
    }

    public ExtendedSearchFormPlugin getExtendedSearchFormPlugin() {
        return extendedSearchFormPlugin;
    }

    public void setExtendedSearchFormPlugin(ExtendedSearchFormPlugin extendedSearchFormPlugin) {
        this.extendedSearchFormPlugin = extendedSearchFormPlugin;
    }

    @Override
    public PluginView createView() {
        ExtendedSearchPluginData initialSearchData = getInitialData();
        return new ExtendedSearchPluginView(this, initialSearchData);
    }

    @Override
    public Component createNew() {
        return new ExtendedSearchPlugin();
    }
}
