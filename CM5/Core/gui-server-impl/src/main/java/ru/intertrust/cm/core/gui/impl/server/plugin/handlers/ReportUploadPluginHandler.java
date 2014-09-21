package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.DeployReportActionContext;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.ReportUploadPluginData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 14:21
 */
@ComponentName("report.upload.plugin")
public class ReportUploadPluginHandler extends PluginHandler {

    public PluginData initialize(Dto config) {
        ReportUploadPluginData pluginData = new ReportUploadPluginData();

        List<ActionContext> activeContexts = new ArrayList<>();
        activeContexts.add(new DeployReportActionContext(PluginHandlerHelper.createActionConfig(
                "deploy-report.action", "deploy-report.action", "Загрузить шаблон отчёта",
                "images/icons/favorite-panel-off.png")));
        pluginData.getToolbarContext().setContexts(activeContexts, ToolbarContext.FacetName.LEFT);
        return pluginData;
    }
}
