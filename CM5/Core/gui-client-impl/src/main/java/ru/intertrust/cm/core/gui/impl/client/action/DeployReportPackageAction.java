package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.plugins.reportupload.ReportPackageImportPlugin;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.DeployReportPackageActionContext;

@ComponentName("deploy-report-package.action")
public class DeployReportPackageAction extends SimpleServerAction {

    @Override
    public Component createNew() {
        return new DeployReportPackageAction();
    }

    @Override
    protected DeployReportPackageActionContext appendCurrentContext(ActionContext initialContext) {
        DeployReportPackageActionContext context = (DeployReportPackageActionContext) initialContext;
        ReportPackageImportPlugin plugin = (ReportPackageImportPlugin) getPlugin();
        context.setAttachmentItem(plugin.getAttachmentItem());
        return context;
    }

    @Override
    protected void onSuccess(ActionData result) {
        final ReportPackageImportPlugin plugin = (ReportPackageImportPlugin) getPlugin();
        plugin.clear();
    }

    @Override
    protected String getDefaultOnSuccessMessage() {
        return LocalizeUtil.get(LocalizationKeys.REPORTS_ARE_UPLOADED_MESSAGE_KEY,
                BusinessUniverseConstants.REPORTS_ARE_UPLOADED_MESSAGE);
    }
}
