package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.gui.form.FormConfig;

/**
 * @author Lesia Puhova
 *         Date: 11.04.14
 *         Time: 11:27
 */
public class ReportFormLogicalValidator {

    private final static Logger logger = LoggerFactory.getLogger(ReportFormLogicalValidator.class);

    public void validate(FormConfig formConfig, LogicalErrors logicalErrors) {
        if (!FormConfig.TYPE_REPORT.equals(formConfig.getType())) {
            return;
        }
        if (formConfig.getReportTemplate() == null || formConfig.getReportTemplate().isEmpty()) {
            String error = String.format("Required attribute 'report-template' is absent in report form '%s'", formConfig.getName());
            logicalErrors.addError(error);
            logger.error(error);
        }
    }
}
