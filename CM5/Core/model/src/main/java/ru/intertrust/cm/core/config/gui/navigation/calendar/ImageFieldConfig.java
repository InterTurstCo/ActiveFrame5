package ru.intertrust.cm.core.config.gui.navigation.calendar;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.config.gui.collection.view.ImageMappingsConfig;

/**
 * @author Sergey.Okolot
 *         Created on 28.10.2014 13:46.
 */
public class ImageFieldConfig implements Dto {

    @Attribute(name = "name")
    private String name;

    @Element(name = "image-mappings")
    private ImageMappingsConfig imageMappingsConfig;

    public String getName() {
        return name;
    }

    public ImageMappingsConfig getImageMappingsConfig() {
        return imageMappingsConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImageFieldConfig that = (ImageFieldConfig) o;

        if (imageMappingsConfig != null ? !imageMappingsConfig.equals(that.imageMappingsConfig) : that
                .imageMappingsConfig != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (imageMappingsConfig != null ? imageMappingsConfig.hashCode() : 0);
        return result;
    }
}
