package ru.intertrust.cm.core.config.gui.collection.view;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.util.ModelConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/02/14
 *         Time: 12:05 PM
 */
@Root(name = "image-mappings")
public class ImageMappingsConfig implements Dto {
    @Attribute(name = "imageWidth", required = true)
    private String imageWidth;
    @Attribute(name = "imageHeight", required = true)
    private String imageHeight;
    @ElementList(entry="mapping", type=MappingConfig.class, inline=true)
    List<MappingConfig> mappingConfigs = new ArrayList<MappingConfig>();


    public String getImageWidth() {
        return imageWidth == null ? ModelConstants.COLLECTION_IMAGE_WIDTH : imageWidth;
    }

    public void setImageWidth(String imageWidth) {
        this.imageWidth = imageWidth;
    }

    public String getImageHeight() {
        return imageHeight == null ? ModelConstants.COLLECTION_IMAGE_HEIGHT : imageHeight;
    }

    public void setImageHeight(String imageHeight) {
        this.imageHeight = imageHeight;
    }

    public List<MappingConfig> getMappingConfigs() {
        return mappingConfigs;
    }

    public void setMappingConfigs(List<MappingConfig> mappingConfigs) {
        this.mappingConfigs = mappingConfigs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ImageMappingsConfig that = (ImageMappingsConfig) o;

        if (mappingConfigs != null ? !mappingConfigs.equals(that.mappingConfigs) : that.mappingConfigs != null) {
            return false;
        }
        if (imageWidth != null ? !imageWidth.equals(that.imageWidth) : that.imageWidth != null) {
            return false;
        }
        if (imageHeight != null ? !imageHeight.equals(that.imageHeight) : that.imageHeight != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = imageWidth != null ? imageWidth.hashCode() : 0;
        result = 31 * result + (imageHeight != null ? imageHeight.hashCode() : 0);
        result = 31 * result + (mappingConfigs != null ? mappingConfigs.hashCode() : 0);
        return result;
    }
}
