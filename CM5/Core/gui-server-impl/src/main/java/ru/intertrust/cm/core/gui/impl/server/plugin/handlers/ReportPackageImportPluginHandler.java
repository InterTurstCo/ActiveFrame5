package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.ActionConfigBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.ReportPackageImportPluginData;

import java.util.HashMap;

@ComponentName("report.package.import.plugin")
public class ReportPackageImportPluginHandler extends PluginHandler {

    @Autowired
    private ActionService actionService;
    @Autowired
    private ActionConfigBuilder actionConfigBuilder;

    public PluginData initialize(Dto config) {
        ReportPackageImportPluginData pluginData = new ReportPackageImportPluginData();
        ToolbarContext toolbarContext = new ToolbarContext();
        ToolBarConfig defaultToolbarConfig = actionService.getDefaultToolbarConfig("report.package.import.plugin",
                GuiContext.getUserLocale());
        actionConfigBuilder.appendConfigs(defaultToolbarConfig.getActions(), new HashMap<String, Object>());
        toolbarContext.setContexts(actionConfigBuilder.getActionContexts(), ToolbarContext.FacetName.LEFT);
        pluginData.setToolbarContext(toolbarContext);
        return pluginData;
    }
}
