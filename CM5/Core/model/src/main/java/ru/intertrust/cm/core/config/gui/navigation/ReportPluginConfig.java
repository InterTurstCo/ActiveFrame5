package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 13:30
 */
public class ReportPluginConfig extends PluginConfig {

    @Attribute(name="report-name", required = false)
    private String reportName;

    @Attribute(name="form-name", required = false)
    private String formName;

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    @Override
    public String getComponentName() {
        return "report.plugin";
    }
}
