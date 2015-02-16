package ru.intertrust.cm.core.config.module;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Text;

/**
 * @author Lesia Puhova
 *         Date: 03.12.14
 *         Time: 17:18
 */
public class LocalizationFileConfiguration {
    @Text
    private String filePath;

    @Attribute(required = false)
    private String locale;

    public String getFilePath() {
        return filePath;
    }

    public String getLocale() {
        return locale;
    }
}
