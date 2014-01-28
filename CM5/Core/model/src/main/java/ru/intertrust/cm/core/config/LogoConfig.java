package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 27.01.14
 *         Time: 17:15
 */
@Root(name = "logo")
public class LogoConfig implements Dto {
    @Attribute(name = "image", required = true)
    private String image;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LogoConfig that = (LogoConfig) o;

        if (image != null ? !image.equals(that.image) : that.image != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return image != null ? image.hashCode() : 0;
    }
}
