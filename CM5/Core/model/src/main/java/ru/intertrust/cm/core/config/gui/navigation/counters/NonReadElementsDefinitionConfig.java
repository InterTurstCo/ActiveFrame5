package ru.intertrust.cm.core.config.gui.navigation.counters;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * Created by
 * Bondarchuk Yaroslav
 * 03.08.2014
 * 12:27
 */
@Root(name = "non-read-elements-definition")
public class NonReadElementsDefinitionConfig implements Dto {
    @Element(name = "non-read-check-collection", required = true)
    private NonReadCheckCollectionConfig nonReadCheckCollectionConfig;

    @Element(name = "object-read-handler", required = true)
    private ObjectReadHandlerConfig objectReadHandlerConfig;

    public NonReadCheckCollectionConfig getNonReadCheckCollectionConfig() {
        return nonReadCheckCollectionConfig;
    }

    public void setNonReadCheckCollectionConfig(NonReadCheckCollectionConfig nonReadCheckCollectionConfig) {
        this.nonReadCheckCollectionConfig = nonReadCheckCollectionConfig;
    }

    public ObjectReadHandlerConfig getObjectReadHandlerConfig() {
        return objectReadHandlerConfig;
    }

    public void setObjectReadHandlerConfig(ObjectReadHandlerConfig objectReadHandlerConfig) {
        this.objectReadHandlerConfig = objectReadHandlerConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NonReadElementsDefinitionConfig that = (NonReadElementsDefinitionConfig) o;

        if (nonReadCheckCollectionConfig != null ? !nonReadCheckCollectionConfig.equals(that.nonReadCheckCollectionConfig)
                : that.nonReadCheckCollectionConfig != null) {
            return false;
        }
        if (objectReadHandlerConfig != null ? !objectReadHandlerConfig.equals(that.objectReadHandlerConfig)
                : that.objectReadHandlerConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = nonReadCheckCollectionConfig != null ? nonReadCheckCollectionConfig.hashCode() : 0;
        result = 31 * result + (objectReadHandlerConfig != null ? objectReadHandlerConfig.hashCode() : 0);
        return result;
    }
}
