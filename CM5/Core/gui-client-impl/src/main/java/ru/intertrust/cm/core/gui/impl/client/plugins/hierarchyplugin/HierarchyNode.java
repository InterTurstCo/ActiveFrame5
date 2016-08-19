package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyCollectionConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyGroupConfig;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.ExpandHierarchyEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.ExpandHierarchyEventHandler;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 29.07.2016
 * Time: 12:20
 * To change this template use File | Settings | File and Code Templates.
 */
public abstract class HierarchyNode extends Composite implements ExpandHierarchyEventHandler,HierarchyPluginConstants {
    protected HierarchyCollectionConfig collectionConfig;
    protected HierarchyGroupConfig groupConfig;
    protected EventBus localBus;
    protected EventBus commonBus;
    protected AbsolutePanel rootPanel;
    protected HorizontalPanel headerPanel;
    protected VerticalPanel childPanel;
    protected HierarchyGuiElementsFactory guiElementsFactory;
    protected HierarchyGuiFactory guiFactory;
    protected Boolean expanded = false;
    protected Id parentId;
    protected String viewID;
    protected String parentViewID;



    protected abstract void addRepresentationCells(Panel container);

    protected HierarchyNode(){
        localBus = new SimpleEventBus();
        guiElementsFactory = new HierarchyGuiElementsFactory();
        guiFactory = new HierarchyGuiFactory();
        rootPanel = new AbsolutePanel();
        headerPanel = new HorizontalPanel();
        childPanel = new VerticalPanel();
    }

    @Override
    public void onExpandHierarchyEvent(ExpandHierarchyEvent event) {
        expanded = event.isExpand();

        if (expanded) {
            for (HierarchyGroupConfig group : (groupConfig!=null)?groupConfig.getHierarchyGroupConfigs():collectionConfig.getHierarchyGroupConfigs()) {
                childPanel.add(guiFactory.buildGroup(group,event.getParentId(),commonBus,getViewID()));
            }
            for (HierarchyCollectionConfig collection : (groupConfig!=null)?groupConfig.getHierarchyCollectionConfigs():collectionConfig.getHierarchyCollectionConfigs()) {
                childPanel.add(guiFactory.buildCollection(collection,event.getParentId(),commonBus,getViewID()));
            }
        } else {
            childPanel.clear();
        }


        childPanel.setVisible(expanded);
    }

    public String getViewID() {
        return viewID;
    }

    public void setViewID(String viewID) {
        this.viewID = viewID;
    }

    public String getParentViewID() {
        return parentViewID;
    }

    public void setParentViewID(String parentViewID) {
        this.parentViewID = parentViewID;
    }
}
