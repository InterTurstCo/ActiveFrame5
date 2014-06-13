package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.config.gui.action.ActionConfig;
import ru.intertrust.cm.core.gui.model.form.FormState;

/**
 * @author Lesia Puhova
 *         Date: 25.03.14
 *         Time: 15:05
 */
public class GenerateReportActionContext extends ActionContext {

    private String reportName;
    private FormState formState;

    public GenerateReportActionContext(){}

    public GenerateReportActionContext(final ActionConfig actionConfig) {
        super(actionConfig);
    }

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public FormState getFormState() {
        return formState;
    }

    public void setFormState(FormState formState) {
        this.formState = formState;
    }

}
