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
}
