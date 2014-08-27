package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.ReportPluginConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.GenerateReportActionContext;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.ReportPluginData;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 14:21
 */
@ComponentName("report.plugin")
public class ReportPluginHandler extends PluginHandler {

    @Autowired
    private GuiService guiService;

    public PluginData initialize(Dto config) {
        ReportPluginConfig reportPluginConfig = (ReportPluginConfig) config;
        String reportName = reportPluginConfig.getReportName();
        String formName = reportPluginConfig.getFormName();

        FormDisplayData formDisplayData = guiService.getReportForm(reportName, formName, GuiContext.get().getUserInfo());

        if (reportName == null) {
            reportName = formDisplayData.getFormState().getRootDomainObjectType();
        }
        ReportPluginData pluginData = new ReportPluginData(reportName, formName, formDisplayData);

        List<ActionContext> activeContexts = new ArrayList<>();
        activeContexts.add(new GenerateReportActionContext(PluginHandlerHelper.createActionConfig(
                "generate-report.action", "generate-report.action", "Создать Отчет", "icons/favorite-panel-off.png")));
        final ToolbarContext toolbarContext = new ToolbarContext();
        toolbarContext.setContexts(activeContexts, ToolbarContext.FacetName.LEFT);
        pluginData.setToolbarContext(toolbarContext);
        return pluginData;
    }
}
