package ru.intertrust.cm.core.gui.impl.client.plugins.extendedsearch;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.impl.client.FormPlugin;
import ru.intertrust.cm.core.gui.impl.client.Plugin;
import ru.intertrust.cm.core.gui.impl.client.PluginPanel;
import ru.intertrust.cm.core.gui.impl.client.PluginView;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.ExtendedSearchPluginData;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/*
* @author IPetrov
*    Date: 08.01.14
*    Time: 13:45
*    Плагин расширенного поиска
*/

@ComponentName("extended.search.plugin")
public class ExtendedSearchPlugin extends Plugin {

    private ExtendedSearchPluginData extendedSearchPluginData;
    private FormPlugin extendedSearchFormPlugin;
    private PluginPanel extendedSearchPluginPanel;

    static Logger log = Logger.getLogger("extended.search.plugin");

    public FormPlugin getExtendedSearchFormPlugin() {
        return extendedSearchFormPlugin;
    }

    public void setExtendedSearchFormPlugin(FormPlugin extendedSearchFormPlugin) {
        this.extendedSearchFormPlugin = extendedSearchFormPlugin;
    }

    public ExtendedSearchPluginData getExtendedSearchPluginData() {
        return extendedSearchPluginData;
    }

    public PluginPanel getExtendedSearchPluginPanel() {
        return extendedSearchPluginPanel;
    }

    public void setExtendedSearchPluginPanel(PluginPanel extendedSearchPluginPanel) {
        this.extendedSearchPluginPanel = extendedSearchPluginPanel;
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

    public void resetFormByInitData(Map<String, WidgetState> extendedSearchConfiguration, List<String> searchAreas, String searchDomainObjectType) {
        ((ExtendedSearchPluginView)getView()).resetFormByInitData(extendedSearchConfiguration, searchAreas, searchDomainObjectType);

    }
}
