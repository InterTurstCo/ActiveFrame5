package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchySurferConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.ActivePluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.ActivePluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginState;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchyPluginData;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchySurferPluginData;
import ru.intertrust.cm.core.gui.model.plugin.hierarchy.HierarchySurferPluginState;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 31.08.2016
 * Time: 11:50
 * To change this template use File | Settings | File and Code Templates.
 */
@ComponentName("hierarchy.surfer.plugin")
public class HierarchySurferHandler extends ActivePluginHandler {

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public ActivePluginData initialize(Dto param) {
        final HierarchySurferConfig config = (HierarchySurferConfig)param;
        final HierarchyPluginHandler hierarchyPluginHandler =
                (HierarchyPluginHandler) applicationContext.getBean("hierarchy.plugin");
        final HierarchyPluginData hierarchyPluginData = hierarchyPluginHandler.initialize(config.getHierarchyPluginConfig());
        final FormPluginConfig formPluginConfig;
        formPluginConfig = new FormPluginConfig("hierarchy_empty_type");

        final FormPluginState fpState = new FormPluginState();
        formPluginConfig.setPluginState(fpState);
        formPluginConfig.setFormViewerConfig(config.getFormViewerConfig());
        fpState.setToggleEdit(false);
        fpState.setEditable(false);
        final FormPluginHandler formPluginHandler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        final FormPluginData formPluginData = formPluginHandler.initialize(formPluginConfig);
        HierarchySurferPluginData result = new HierarchySurferPluginData();
        result.setFormPluginData(formPluginData);
        result.setHierarchyPluginData(hierarchyPluginData);
        HierarchySurferPluginState pluginState = new HierarchySurferPluginState();
        result.setPluginState(pluginState);

        return result;
    }
}
