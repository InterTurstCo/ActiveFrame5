package ru.intertrust.cm.core.gui.model.action.system;

import ru.intertrust.cm.core.config.gui.navigation.CollectionViewerConfig;

/**
 * @author Sergey.Okolot
 *         Created on 04.08.2014 14:56.
 */
public class CollectionFiltersActionContext extends AbstractUserSettingActionContext {
    public static final String COMPONENT_NAME = "collection.filters.action";

    private CollectionViewerConfig collectionViewerConfig;

    public CollectionViewerConfig getCollectionViewerConfig() {
        return collectionViewerConfig;
    }

    public void setCollectionViewerConfig(CollectionViewerConfig collectionViewerConfig) {
        this.collectionViewerConfig = collectionViewerConfig;
    }
}
