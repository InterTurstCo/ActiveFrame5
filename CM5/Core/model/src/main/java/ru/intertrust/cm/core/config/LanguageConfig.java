package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.base.Localizable;

/**
 * @author Lesia Puhova
 *         Date: 03.03.2015
 *         Time: 16:48
 */
@Root(name = "language")
public class LanguageConfig implements Dto {
    @Attribute(name = "name", required = true)
    private String name;

    @Attribute(name = "display-name", required = true)
    @Localizable
    private String displayName;

    @Attribute(name = "img", required = false)
    private String img;

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getImg() {
        return img;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LanguageConfig that = (LanguageConfig) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null) return false;
        if (img != null ? !img.equals(that.img) : that.img != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}
