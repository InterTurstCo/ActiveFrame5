package ru.intertrust.cm.core.gui.model.plugin;

import ru.intertrust.cm.core.config.gui.navigation.DomainObjectSurferConfig;
import ru.intertrust.cm.core.config.gui.navigation.LinkConfig;
import ru.intertrust.cm.core.gui.model.action.ToolbarContext;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionPluginData;

public class DomainObjectSurferPluginData extends ActivePluginData {
    private CollectionPluginData collectionPluginData;
    private FormPluginData formPluginData;
    private DomainObjectSurferConfig domainObjectSurferConfig;
    private Integer splitterPosition;
    /**
     * 0 - горизонтальная, 1 - вертикальная
     */
    private Integer splitterOrientation;
    private LinkConfig hierarchicalLink;


    public DomainObjectSurferConfig getDomainObjectSurferConfig() {
        return domainObjectSurferConfig;
    }

    public void setDomainObjectSurferConfig(DomainObjectSurferConfig domainObjectSurferConfig) {
        this.domainObjectSurferConfig = domainObjectSurferConfig;
    }

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

    public Integer getSplitterPosition() {
        return splitterPosition;
    }

    public void setSplitterPosition(Integer splitterPosition) {
        this.splitterPosition = splitterPosition;
    }

    public Integer getSplitterOrientation() {
        return splitterOrientation;
    }

    public void setSplitterOrientation(Integer splitterOrientation) {
        this.splitterOrientation = splitterOrientation;
    }

    public LinkConfig getHierarchicalLink() {
        return hierarchicalLink;
    }

    public void setHierarchicalLink(LinkConfig hierarchicalLink) {
        this.hierarchicalLink = hierarchicalLink;
    }

    @Override
    public ToolbarContext getToolbarContext() {
        final ToolbarContext result = new ToolbarContext();
        result.copyToolbar(collectionPluginData.getToolbarContext());
        result.mergeToolbar(formPluginData.getToolbarContext());
        return result;
    }
}
