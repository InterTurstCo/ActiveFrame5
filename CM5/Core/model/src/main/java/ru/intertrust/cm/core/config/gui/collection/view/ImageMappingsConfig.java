package ru.intertrust.cm.core.config.gui.collection.view;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 10/02/14
 *         Time: 12:05 PM
 */
@Root(name = "image-mappings")
public class ImageMappingsConfig implements Dto {
    @ElementList(entry="mapping", type=MappingConfig.class, inline=true)
    List<MappingConfig> mappingConfigs = new ArrayList<MappingConfig>();

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

        return true;
    }

    @Override
    public int hashCode() {
        return mappingConfigs != null ? mappingConfigs.hashCode() : 0;
    }
}
