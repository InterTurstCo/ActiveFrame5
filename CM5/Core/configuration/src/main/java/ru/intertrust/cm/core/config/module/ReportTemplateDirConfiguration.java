package ru.intertrust.cm.core.config.module;

import org.simpleframework.xml.Text;

/**
 * @author Lesia Puhova
 *         Date: 27.03.14
 *         Time: 16:23
 */
public class ReportTemplateDirConfiguration {

    @Text
    private String templateDirPath;

    public String getTemplateDirPath() {
        return templateDirPath;
    }

    public void setTemplateDirPath(String templateDirPath) {
        this.templateDirPath = templateDirPath;
    }
}
