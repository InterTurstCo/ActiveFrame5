package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.gui.model.form.FormDisplayData;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 14:51
 */
public class ReportPluginData extends ActivePluginData {

    private String reportName;
    private String formName;
    private FormDisplayData formDisplayData;

    public ReportPluginData(){} // for GWT serialization support only; normally should not be used.

    public ReportPluginData(String reportName, String formName, FormDisplayData formDisplayData) {
        this.reportName = reportName;
        this.formName = formName;
        this.formDisplayData = formDisplayData;
    }

    public String getReportName() {
        return reportName;
    }

    public String getFormName() {
        return formName;
    }

    public FormDisplayData getFormDisplayData() {
        return formDisplayData;
    }
}
