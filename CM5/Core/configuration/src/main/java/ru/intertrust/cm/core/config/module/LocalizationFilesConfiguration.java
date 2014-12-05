package ru.intertrust.cm.core.config.module;

import org.simpleframework.xml.ElementList;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 03.12.14
 *         Time: 17:14
 */
public class LocalizationFilesConfiguration {

    @ElementList(inline=true, required=false, entry="localization-file")
    private List<LocalizationFileConfiguration> localizationFiles = new ArrayList<>();

    public List<LocalizationFileConfiguration> getLocalizationFiles() {
        return localizationFiles;
    }
}
