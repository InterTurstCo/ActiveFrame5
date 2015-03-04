package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */
@Root(name = "settings-popup")
public class SettingsPopupConfig implements Dto {
    @Element(name = "themes", required = false)
    private ThemesConfig themesConfig;

    @Element(name = "languages", required = false)
    private LanguagesConfig languagesConfig;

    public ThemesConfig getThemesConfig() {
        return themesConfig;
    }

    public void setThemesConfig(ThemesConfig themesConfig) {
        this.themesConfig = themesConfig;
    }

    public LanguagesConfig getLanguagesConfig() {
        return languagesConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SettingsPopupConfig that = (SettingsPopupConfig) o;
        if (themesConfig != null ? !themesConfig.equals(that.themesConfig) : that.themesConfig != null) {
            return false;
        }
        if (languagesConfig != null ? !languagesConfig.equals(that.languagesConfig) : that.languagesConfig != null) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = themesConfig != null ? themesConfig.hashCode() : 17;
        result = 31 * result + (languagesConfig != null ? languagesConfig.hashCode() : 17);
        return result;
    }
}
