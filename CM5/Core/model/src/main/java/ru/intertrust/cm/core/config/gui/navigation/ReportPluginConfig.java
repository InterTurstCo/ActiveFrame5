package ru.intertrust.cm.core.config.gui.navigation;

import org.simpleframework.xml.Attribute;

/**
 * @author Lesia Puhova
 *         Date: 18.03.14
 *         Time: 13:30
 */
public class ReportPluginConfig extends PluginConfig {

    @Attribute(name="report-name", required = true)
    private String reportName;

    public String getReportName() {
        return reportName;
    }

    public void setReportName(String reportName) {
        this.reportName = reportName;
    }

    @Override
    public String getComponentName() {
        return "report.plugin";
    }
}
