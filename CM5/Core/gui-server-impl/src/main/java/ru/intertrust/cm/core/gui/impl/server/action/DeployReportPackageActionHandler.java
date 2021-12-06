package ru.intertrust.cm.core.gui.impl.server.action;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.PropertyResolver;
import ru.intertrust.cm.core.business.api.ReportServiceAdmin;
import ru.intertrust.cm.core.business.api.extensions.AfterUploadReportPackageExtension;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.model.ComponentName;
import ru.intertrust.cm.core.gui.model.action.ActionData;
import ru.intertrust.cm.core.gui.model.action.DeployReportPackageActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;
import ru.intertrust.cm.core.gui.model.form.widget.AttachmentItem;

import javax.xml.bind.UnmarshalException;
import java.io.*;

@ComponentName("deploy-report-package.action")
public class DeployReportPackageActionHandler extends ActionHandler<DeployReportPackageActionContext, ActionData> {
    private static final Logger logger = LoggerFactory.getLogger(DeployReportPackageActionHandler.class);

    @Autowired
    private ReportServiceAdmin reportServiceAdmin;
    @Autowired
    private PropertyResolver propertyResolver;
    @Autowired
    private ExtensionService extensionService;

    private static final String TEMP_STORAGE_PATH = "${attachment.temp.storage}";

    @Override
    public ActionData executeAction(DeployReportPackageActionContext deployContext) {
        logMessage("executeAction >>>");
        AttachmentItem attachmentItem = deployContext.getAttachmentItem();
        if (attachmentItem != null) {
            try {
                importReportPackage(attachmentItem);
            } catch (UnmarshalException e) {
                logger.error("Ошибка при загрузке и импорте пакета отчетов (Unmarshal).", e);
                String errorMessage = e.getLinkedException() != null && e.getLinkedException().getMessage() != null ?
                        e.getLinkedException().getMessage() : e.getCause() != null && e.getCause().getMessage() != null ?
                        e.getCause().getMessage() : e.getMessage();
                throw new RuntimeException("Ошибка при загрузке и импорте пакета отчетов (" + errorMessage + ").",
                        e.getLinkedException() != null ? e.getLinkedException() : e.getCause() != null ? e.getCause() : e);
            } catch (Exception e) {
                logger.error("Ошибка при загрузке и импорте пакета отчетов (Exception).", e);
                String errorMessage = e.getCause() != null && e.getCause().getMessage() != null ?
                        e.getCause().getMessage() : e.getMessage();
                throw new RuntimeException("Ошибка при загрузке и импорте пакета отчетов (" + errorMessage + ").",
                        e.getCause() != null ? e.getCause() : e);
            } catch (Throwable e) {
                logger.error("Ошибка при загрузке и импорте пакета отчетов (Throwable).", e);
                throw new RuntimeException("Ошибка при загрузке и импорте пакета отчетов (" + e.getMessage() + ").", e);
            }
        } else {
            throw new RuntimeException("Не указан пакет отчетов.");
        }
        logMessage("executeAction <<<");
        return new SimpleActionData();
    }

    @Override
    public DeployReportPackageActionContext getActionContext(final ActionConfig actionConfig) {
        return new DeployReportPackageActionContext(actionConfig);
    }

    private void importReportPackage(AttachmentItem reportPackage) throws Exception {
        logMessage("importReportPackage >>>");
        String pathForTempFilesStore = propertyResolver.resolvePlaceholders(TEMP_STORAGE_PATH);
        logMessage("importReportPackage === 1", "pathForTempFilesStore=", pathForTempFilesStore);
        File jarFile = new File(pathForTempFilesStore, reportPackage.getTemporaryName());
        logMessage("importReportPackage === 2", "jarFile=", jarFile != null ? jarFile.getAbsolutePath() : null);
        reportServiceAdmin.importReportPackage(jarFile);

        callExtensions(jarFile);
        logMessage("importReportPackage <<<");
    }

    private void callExtensions(File jarFile) {
        AfterUploadReportPackageExtension ep = extensionService.getExtensionPoint(AfterUploadReportPackageExtension.class, null);
        ep.execute(jarFile);
    }

    private static void logMessage(Object... data) {
        if (logger.isDebugEnabled()) {
            String logMsg = "";
            if (data != null) {
                for (Object msg : data) {
                    logMsg += (msg != null ? msg.toString() : "null") + " ";
                }
            } else {
                logMsg = "null";
            }
            logger.debug(logMsg);
        }
    }
}
