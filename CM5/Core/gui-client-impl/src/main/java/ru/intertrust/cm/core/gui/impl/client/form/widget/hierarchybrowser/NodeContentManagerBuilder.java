package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.HierarchyBrowserConfig;
import ru.intertrust.cm.core.config.gui.form.widget.NodeCollectionDefConfig;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetsContainer;
import ru.intertrust.cm.core.gui.model.filters.WidgetIdComponentName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.02.2015
 *         Time: 11:08
 */
public class NodeContentManagerBuilder {
    private HierarchyBrowserConfig config;
    private Map<String, NodeCollectionDefConfig> collectionNameNodeMap;
    private HierarchyBrowserMainPopup mainPopup;
    private Id parentId;
    private ArrayList<Id> chosenIds = new ArrayList<Id>();
    private WidgetsContainer widgetsContainer;
    private Collection<WidgetIdComponentName> widgetIdComponentNames;
    private String collectionName;
    protected String inputText;
    private int offset;

    public NodeContentManagerBuilder withConfig(HierarchyBrowserConfig config) {
        this.config = config;
        return this;
    }

    public NodeContentManagerBuilder withCollectionNameNodeMap(Map<String, NodeCollectionDefConfig> collectionNameNodeMap) {
        this.collectionNameNodeMap = collectionNameNodeMap;
        return this;
    }

    public NodeContentManagerBuilder withMainPopup(HierarchyBrowserMainPopup mainPopup) {
        this.mainPopup = mainPopup;
        return this;
    }

    public NodeContentManagerBuilder withParentId(Id parentId) {
        this.parentId = parentId;
        return this;
    }

    public NodeContentManagerBuilder withChosenIds(ArrayList<Id> chosenIds) {
        this.chosenIds = chosenIds;
        return this;
    }

    public NodeContentManagerBuilder withWidgetsContainer(WidgetsContainer widgetsContainer) {
        this.widgetsContainer = widgetsContainer;
        return this;
    }

    public NodeContentManagerBuilder withWidgetIdComponentNames(Collection<WidgetIdComponentName> widgetIdComponentNames) {
        this.widgetIdComponentNames = widgetIdComponentNames;
        return this;
    }

    public NodeContentManagerBuilder  withCollectionName(String collectionName) {
        this.collectionName = collectionName;
        return this;
    }

    public NodeContentManagerBuilder withOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public NodeContentManagerBuilder withInputText(String inputText) {
        this.inputText = inputText;
        return this;
    }

    public NodeContentManager buildFirstNodeContentManager(){
       return new FirstNodeContentManager(config,mainPopup, chosenIds, collectionNameNodeMap, widgetsContainer,
                widgetIdComponentNames);

    }
    public NodeContentManager buildRefreshNodeContentManager(){
        return new RefreshNodeContentManager(config,mainPopup, chosenIds, collectionName, parentId, inputText,
                collectionNameNodeMap, widgetsContainer,widgetIdComponentNames);

    }
    public NodeContentManager buildScrollNodeContentManager(){
        return new ScrollNodeContentManager(config,mainPopup, chosenIds, collectionName, parentId, inputText, offset,
                collectionNameNodeMap, widgetsContainer, widgetIdComponentNames);

    }
    public NodeContentManager buildNewNodeContentManager(){
        return new NewNodeContentManager(config,mainPopup, chosenIds, collectionName, parentId,
                collectionNameNodeMap, widgetsContainer, widgetIdComponentNames);

    }
}
