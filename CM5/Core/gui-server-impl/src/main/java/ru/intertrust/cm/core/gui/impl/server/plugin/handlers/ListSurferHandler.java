package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.listplugin.ListSurferConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.listplugin.ListPluginData;
import ru.intertrust.cm.core.gui.model.plugin.listplugin.ListSurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.listplugin.ListSurferPluginState;

/**
 * Created by Ravil on 17.04.2017.
 */
@ComponentName("list.surfer.plugin")
public class ListSurferHandler extends ActivePluginHandler {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public ActivePluginData initialize(Dto param) {
        ListSurferConfig config =(ListSurferConfig) param;
        final ListPluginHandler listPluginHandler =
                (ListPluginHandler) applicationContext.getBean("list.plugin");
        final ListPluginData listPluginData = listPluginHandler.initialize(config.getListPluginConfig());
        final FormPluginConfig formPluginConfig;
        formPluginConfig = new FormPluginConfig("hierarchy_empty_type");

        final FormPluginState fpState = new FormPluginState();
        formPluginConfig.setPluginState(fpState);
        formPluginConfig.setFormViewerConfig(config.getFormViewerConfig());
        fpState.setToggleEdit(false);
        fpState.setEditable(false);
        final FormPluginHandler formPluginHandler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        final FormPluginData formPluginData = formPluginHandler.initialize(formPluginConfig);
        ListSurferPluginData result = new ListSurferPluginData();
        result.setFormPluginData(formPluginData);
        result.setListPluginData(listPluginData);
        ListSurferPluginState pluginState = new ListSurferPluginState();
        result.setPluginState(pluginState);
        return result;
    }
}
