package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.config.model.base.CollectionConfig;
import ru.intertrust.cm.core.config.model.gui.collection.view.CollectionViewConfig;

import java.util.List;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 17/9/13
 *         Time: 12:05 PM
 */
public class CollectionPluginData extends PluginData {
    private String collectionName;

    private CollectionConfig collectionConfig;

    private CollectionViewConfig collectionViewConfig;

    private List<String> columnNames;

    private List<List<String>> stringList;

    public CollectionPluginData() {

    }

    public List<List<String>> getStringList() {
        return stringList;
    }

    public void setStringList(List<List<String>> stringList) {
        this.stringList = stringList;
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

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }
}
