package ru.intertrust.cm.core.config;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.List;

/**
 * @author Lesia Puhova
 *         Date: 03.03.2015
 *         Time: 16:54
 */
@Root(name = "languages")
public class LanguagesConfig implements Dto {
    @ElementList(inline = true, name ="language", required = true)
    private List<LanguageConfig> languageConfigs;

    private String selectedLanguage;

    public List<LanguageConfig> getLanguageConfigs() {
        return languageConfigs;
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof LanguagesConfig)) {
            return false;
        }
        LanguagesConfig that = (LanguagesConfig) obj;
        if (languageConfigs != null ? !languageConfigs.equals(that.languageConfigs) : that.languageConfigs != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return languageConfigs != null ? languageConfigs.hashCode() : 17;
    }
}
