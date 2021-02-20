package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.DeployReportPackageActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@ComponentName("deploy-report-package.action")
public class DeployReportPackageActionHandler extends ActionHandler<DeployReportPackageActionContext, ActionData> {

    @Autowired
    private ReportServiceAdmin reportServiceAdmin;

    @Autowired
    private PropertyResolver propertyResolver;

    private static final String TEMP_STORAGE_PATH = "${attachment.temp.storage}";

    @Override
    public ActionData executeAction(DeployReportPackageActionContext deployContext) {
        AttachmentItem attachmentItem = deployContext.getAttachmentItem();
        if (attachmentItem != null) {
            // TODO распаковка архива и загрузка отчетов
        } else {
            throw new RuntimeException("There is no report package.");
        }
        return new SimpleActionData();
    }

    @Override
    public DeployReportPackageActionContext getActionContext(final ActionConfig actionConfig) {
        return new DeployReportPackageActionContext(actionConfig);
    }

    protected byte[] readFile(File file) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        FileInputStream input = null;
        try {
            input = new FileInputStream(file);
            int read = 0;
            byte[] buffer = new byte[1024];
            while ((read = input.read(buffer)) > 0) {
                out.write(buffer, 0, read);
            }
            return out.toByteArray();
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }
}
