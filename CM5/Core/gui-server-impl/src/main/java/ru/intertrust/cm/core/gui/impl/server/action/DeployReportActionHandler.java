package ru.intertrust.cm.core.gui.impl.server.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.dto.DeployReportData;
import ru.intertrust.cm.core.business.api.dto.DeployReportItem;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.DeployReportActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 14.04.14
 *         Time: 15:20
 */
@ComponentName("deploy-report.action")
public class DeployReportActionHandler extends ActionHandler<DeployReportActionContext, ActionData> {

    @Autowired
    private ReportServiceAdmin reportServiceAdmin;

    @Autowired
    private PropertyResolver propertyResolver;

    private static final String TEMP_STORAGE_PATH = "${attachment.temp.storage}";

    @Override
    public ActionData executeAction(DeployReportActionContext deployContext) {
        List<AttachmentItem> attachmentItems = deployContext.getAttachmentItems();
        DeployReportData deployData = new DeployReportData();
        for (AttachmentItem attachmentItem : attachmentItems) {
            String pathForTempFilesStore = propertyResolver.resolvePlaceholders(TEMP_STORAGE_PATH);
            File file = new File(pathForTempFilesStore, attachmentItem.getTemporaryName());
            try  {
                DeployReportItem deployItem = new DeployReportItem();
                deployItem.setName(attachmentItem.getName());
                deployItem.setBody(readFile(file));

                deployData.getItems().add(deployItem);
            } catch (IOException e) {
                e.printStackTrace(); //TODO: handle exception
            }
        }
        reportServiceAdmin.deploy(deployData, true);

        return new SimpleActionData();
    }

    @Override
    public DeployReportActionContext getActionContext(final ActionConfig actionConfig) {
        return new DeployReportActionContext(actionConfig);
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
            input.close();
        }
    }
}
