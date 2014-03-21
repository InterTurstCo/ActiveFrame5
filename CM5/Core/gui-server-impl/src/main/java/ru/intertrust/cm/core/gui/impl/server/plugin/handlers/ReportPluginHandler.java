package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.ReportPluginConfig;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.ActionConfigBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.SaveToCSVContext;
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

        FormDisplayData formDisplayData = guiService.getReportForm(reportName, GuiContext.get().getUserInfo());
        ReportPluginData pluginData = new ReportPluginData(reportName, formDisplayData);

        List<ActionContext> activeContexts = new ArrayList<ActionContext>();
        activeContexts.add(new SaveToCSVContext(ActionConfigBuilder.createActionConfig(
                "generate-report.action", "generate-report.action", "Создать Отчет", "icons/favorite-panel-off.png")));
        pluginData.setActionContexts(activeContexts);

        return pluginData;
    }
}
