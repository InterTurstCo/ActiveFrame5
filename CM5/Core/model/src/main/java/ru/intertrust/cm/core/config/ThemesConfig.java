package ru.intertrust.cm.core.config;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.List;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */
@Root(name = "themes")
public class ThemesConfig implements Dto {
    @ElementList(inline = true, name ="theme", required = true)
    private List<ThemeConfig> themes;

    public List<ThemeConfig> getThemes() {
        return themes;
    }

    public void setThemes(List<ThemeConfig> themes) {
        this.themes = themes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof ThemesConfig)) {
            return false;
        }

        ThemesConfig that = (ThemesConfig) obj;

        if (themes != null ? !themes.equals(that.themes) : that.themes != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return themes != null ? themes.hashCode() : 17;
    }
}
