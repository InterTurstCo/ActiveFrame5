package ru.intertrust.cm.core.config;

import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

/**
 * Представляет Java модель для конфигурации коллекций.
 * @author atsvetkov
 *
 */

@Root
public class CollectionConfiguration {

    @ElementList(inline = true)
    private List<CollectionConfig> collectionConfigs;

    public List<CollectionConfig> getCollectionConfigs() {
        return collectionConfigs;
    }

    public void setCollectionConfigs(List<CollectionConfig> collectionConfigs) {
        this.collectionConfigs = collectionConfigs;
    }
    
}
