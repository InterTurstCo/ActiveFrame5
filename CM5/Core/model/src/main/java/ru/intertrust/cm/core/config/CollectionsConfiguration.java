package ru.intertrust.cm.core.config;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.io.Serializable;
import java.util.List;

/**
 * Представляет Java модель для конфигурации коллекций.
 * @author atsvetkov
 *
 */

@Root
public class CollectionsConfiguration implements Serializable {

    @ElementList(inline = true)
    private List<CollectionConfig> collectionConfigs;

    public List<CollectionConfig> getCollectionConfigs() {
        return collectionConfigs;
    }

    public void setCollectionConfigs(List<CollectionConfig> collectionConfigs) {
        this.collectionConfigs = collectionConfigs;
    }
}
