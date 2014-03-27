package ru.intertrust.cm.core.config.module;

import org.simpleframework.xml.Text;

/**
 * @author Lesia Puhova
 *         Date: 27.03.14
 *         Time: 19:59
 */

public class ReportTemplateFileConfiguration {

    @Text
    private String templateFilePath;

    public String getTemplateFilePath() {
        return templateFilePath;
    }

    public void setTemplateFilePath(String templateFilePath) {
        this.templateFilePath = templateFilePath;
    }
}
