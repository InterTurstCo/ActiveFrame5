package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.navigation.ReportPluginConfig;
import ru.intertrust.cm.core.gui.api.server.ActionService;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.plugin.PluginHandler;
import ru.intertrust.cm.core.gui.impl.server.util.ActionConfigBuilder;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.ReportPluginData;

import java.util.HashMap;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 14:21
 */
@ComponentName("report.plugin")
public class ReportPluginHandler extends PluginHandler {

    @Autowired
    private GuiService guiService;
    @Autowired
    private ActionService actionService;
    @Autowired
    private ActionConfigBuilder actionConfigBuilder;
    @Autowired
    private ProfileService profileService;

    public PluginData initialize(Dto config) {
        ReportPluginConfig reportPluginConfig = (ReportPluginConfig) config;
        String reportName = reportPluginConfig.getReportName();
        String formName = reportPluginConfig.getFormName();

        FormDisplayData formDisplayData = guiService.getReportForm(reportName, formName, GuiContext.get().getUserInfo());

        if (reportName == null) {
            reportName = formDisplayData.getFormState().getRootDomainObjectType();
        }
        ReportPluginData pluginData = new ReportPluginData(reportName, formName, formDisplayData);

        ToolbarContext toolbarContext = new ToolbarContext();
        ToolBarConfig defaultToolbarConfig = actionService.getDefaultToolbarConfig(reportPluginConfig.getComponentName(),
                profileService.getPersonLocale());
        actionConfigBuilder.appendConfigs(defaultToolbarConfig.getActions(), new HashMap<String, Object>());
        toolbarContext.setContexts(actionConfigBuilder.getActionContexts(), ToolbarContext.FacetName.LEFT);

        pluginData.setToolbarContext(toolbarContext);
        return pluginData;
    }


}
