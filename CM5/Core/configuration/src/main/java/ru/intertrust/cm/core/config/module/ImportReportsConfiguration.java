package ru.intertrust.cm.core.config.module;

import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 27.03.14
 *         Time: 16:33
 */
public class ImportReportsConfiguration {

    @ElementList(inline=true, required=false, entry="report-template-dir")
    private List<ReportTemplateDirConfiguration> reportTemplateDirs = new ArrayList<ReportTemplateDirConfiguration>();

    @ElementList(inline=true, required=false, entry="report-template")
    private List<ReportTemplateConfiguration> reportTemplates = new ArrayList<ReportTemplateConfiguration>();

    public List<ReportTemplateDirConfiguration> getReportTemplateDirs() {
        return reportTemplateDirs;
    }

    public void setReportTemplateDirs(List<ReportTemplateDirConfiguration> reportTemplateDirs) {
        this.reportTemplateDirs = reportTemplateDirs;
    }

    public List<ReportTemplateConfiguration> getReportTemplates() {
        return reportTemplates;
    }

    public void setReportTemplates(List<ReportTemplateConfiguration> reportTemplates) {
        this.reportTemplates = reportTemplates;
    }
}
