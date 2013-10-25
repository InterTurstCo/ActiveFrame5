package ru.intertrust.cm.core.gui.model.plugin;

public class DomainObjectSurferPluginData extends ActivePluginData {
    private CollectionPluginData collectionPluginData;
    private FormPluginData formPluginData;

    public CollectionPluginData getCollectionPluginData() {
        return collectionPluginData;
    }

    public void setCollectionPluginData(CollectionPluginData collectionPluginData) {
        this.collectionPluginData = collectionPluginData;
    }

    public FormPluginData getFormPluginData() {
        return formPluginData;
    }

    public void setFormPluginData(FormPluginData formPluginData) {
        this.formPluginData = formPluginData;
    }
}
