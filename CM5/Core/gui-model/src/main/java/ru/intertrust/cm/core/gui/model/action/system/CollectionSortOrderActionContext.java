package ru.intertrust.cm.core.gui.model.action.system;

import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;

/**
 * @author Sergey.Okolot
 *         Created on 31.07.2014 18:38.
 */
public class CollectionSortOrderActionContext extends AbstractUserSettingActionContext {

    public static final String COMPONENT_NAME = "collection.sort.order.action";

    private CollectionViewerConfig collectionViewerConfig;

    public CollectionViewerConfig getCollectionViewerConfig() {
        return collectionViewerConfig;
    }

    public void setCollectionViewerConfig(CollectionViewerConfig collectionViewerConfig) {
        this.collectionViewerConfig = collectionViewerConfig;
    }
}
