package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.navigation.ReportPluginConfig;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.ReportPluginData;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 14:21
 */
@ComponentName("report.plugin")
public class ReportPluginHandler extends PluginHandler {

    public PluginData initialize(Dto config) {
        ReportPluginConfig reportPluginConfig = (ReportPluginConfig) config;
        String reportName = reportPluginConfig.getReportName();

        ReportPluginData pluginData = new ReportPluginData();
        pluginData.setReportName(reportName);
        return pluginData;
    }
}
