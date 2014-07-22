package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 19.05.14
 *         Time: 17:15
 */
@Root(name = "theme")
public class ThemeConfig implements Dto {
    @Attribute(name = "display-name", required = true)
    private String displayName;

    @Attribute(name = "component-name", required = true)
    private String componentName;

    @Attribute(name = "img", required = false)
    private String img;

    @Attribute(name = "default-theme", required = false)
    private boolean defaultTheme;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public boolean isDefaultTheme() {
        return defaultTheme;
    }

    public void setDefaultTheme(boolean defaultTheme) {
        this.defaultTheme = defaultTheme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ThemeConfig that = (ThemeConfig) o;

        if (defaultTheme != that.defaultTheme) {
            return false;
        }
        if (img != null ? !img.equals(that.img) : that.img != null) {
            return false;
        }
        if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null) {
            return false;
        }
        if (componentName != null ? !componentName.equals(that.componentName) : that.componentName != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = displayName != null ? displayName.hashCode() : 31;
        result = 31 * result + (componentName!= null ? componentName.hashCode() : 31);
        result = 31 * result + (img != null ? img.hashCode() : 31);
        result = 31 * result + (defaultTheme ? 1 : 31);
        return result;
    }
}
