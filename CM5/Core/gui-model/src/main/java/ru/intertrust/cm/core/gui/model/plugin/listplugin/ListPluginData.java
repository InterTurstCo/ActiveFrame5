package ru.intertrust.cm.core.gui.model.plugin.listplugin;

import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

import java.util.List;

/**
 * Created by Ravil on 11.04.2017.
 */
public class ListPluginData extends PluginData {
    private List<CollectionRowItem> collectionRowItems;

    public List<CollectionRowItem> getCollectionRowItems() {
        return collectionRowItems;
    }

    public void setCollectionRowItems(List<CollectionRowItem> collectionRowItems) {
        this.collectionRowItems = collectionRowItems;
    }
}
