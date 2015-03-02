package ru.intertrust.cm.core.gui.impl.client.action;

import ru.intertrust.cm.core.gui.api.client.Component;
import ru.intertrust.cm.core.gui.api.client.LocalizeUtil;
import ru.intertrust.cm.core.gui.impl.client.plugins.reportupload.ReportUploadPlugin;
import ru.intertrust.cm.core.gui.impl.client.util.BusinessUniverseConstants;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionContext;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.DeployReportActionContext;

/**
 * @author Lesia Puhova
 *         Date: 14.04.14
 *         Time: 13:16
 */
@ComponentName("deploy-report.action")
public class DeployReportAction extends SimpleServerAction {

    @Override
    public Component createNew() {
        return new DeployReportAction();
    }

    @Override
    protected DeployReportActionContext appendCurrentContext(ActionContext initialContext) {
        DeployReportActionContext context = (DeployReportActionContext) initialContext;
        ReportUploadPlugin plugin = (ReportUploadPlugin) getPlugin();
        context.setAttachmentItems(plugin.getAttachmentItems());
        return context;
    }

    @Override
    protected void onSuccess(ActionData result) {
        final ReportUploadPlugin plugin = (ReportUploadPlugin) getPlugin();
        plugin.clear();
    }

    @Override
    protected String getDefaultOnSuccessMessage() {
        return LocalizeUtil.get(BusinessUniverseConstants.REPORT_IS_UPLOADED_MESSAGE);
    }
}
