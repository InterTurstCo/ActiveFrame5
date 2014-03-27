package ru.intertrust.cm.core.config.module;

import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 27.03.14
 *         Time: 16:20
 */
public class ReportTemplateConfiguration {

    @ElementList(inline=true, required=false, entry="template-file")
    private List<ReportTemplateFileConfiguration> templatePaths = new ArrayList<ReportTemplateFileConfiguration>();

    public List<ReportTemplateFileConfiguration> getTemplatePaths() {
        return templatePaths;
    }

    public void setTemplatePaths(List<ReportTemplateFileConfiguration> templatePaths) {
        this.templatePaths = templatePaths;
    }
}
