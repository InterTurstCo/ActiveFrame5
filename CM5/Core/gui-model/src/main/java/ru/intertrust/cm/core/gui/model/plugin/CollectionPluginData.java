package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.business.api.dto.IdentifiableObjectCollection;
import ru.intertrust.cm.core.config.model.base.CollectionConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionViewConfig;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginData extends PluginData {

    private String collectionName;

    private CollectionConfig collectionConfig;

    private CollectionViewConfig collectionViewConfig;

    private IdentifiableObjectCollection identifiableObjects;

    public CollectionPluginData() {

    }

    public IdentifiableObjectCollection getCollection() {
        return identifiableObjects;
    }

    public void setCollection(IdentifiableObjectCollection identifiableObjects) {
        this.identifiableObjects = identifiableObjects;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public CollectionConfig getCollectionConfig() {
        return collectionConfig;
    }

    public void setCollectionConfig(CollectionConfig collectionConfig) {
        this.collectionConfig = collectionConfig;
    }

    public CollectionViewConfig getCollectionViewConfig() {
        return collectionViewConfig;
    }

    public void setCollectionViewConfig(CollectionViewConfig collectionViewConfig) {
        this.collectionViewConfig = collectionViewConfig;
    }

}
