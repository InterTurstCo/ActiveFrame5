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
import ru.intertrust.cm.core.gui.model.plugin.ReportUploadPluginData;

import java.util.HashMap;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 14:21
 */
@ComponentName("report.upload.plugin")
public class ReportUploadPluginHandler extends PluginHandler {

    @Autowired
    private ActionService actionService;
    @Autowired
    private ActionConfigBuilder actionConfigBuilder;

    public PluginData initialize(Dto config) {
        ReportUploadPluginData pluginData = new ReportUploadPluginData();
        ToolbarContext toolbarContext = new ToolbarContext();
        ToolBarConfig defaultToolbarConfig = actionService.getDefaultToolbarConfig("report.upload.plugin",
                GuiContext.getUserLocale());
        actionConfigBuilder.appendConfigs(defaultToolbarConfig.getActions(), new HashMap<String, Object>());
        toolbarContext.setContexts(actionConfigBuilder.getActionContexts(), ToolbarContext.FacetName.LEFT);
        pluginData.setToolbarContext(toolbarContext);
        return pluginData;
    }
}
