package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.config.model.base.CollectionConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionViewConfig;

import java.util.Collection;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginData extends PluginData {

    private Collection<CollectionConfig> collectionConfigs;

    private Collection<CollectionViewConfig> collectionViewConfigs;

    public CollectionPluginData() {

    }

    public Collection<CollectionConfig> getCollectionConfigs() {
        return collectionConfigs;
    }

    public void setCollectionConfigs(Collection<CollectionConfig> collectionConfigs) {
        this.collectionConfigs = collectionConfigs;
    }

    public Collection<CollectionViewConfig> getCollectionViewConfigs() {
        return collectionViewConfigs;
    }

    public void setCollectionViewConfigs(Collection<CollectionViewConfig> collectionViewConfigs) {
        this.collectionViewConfigs = collectionViewConfigs;
    }
}
