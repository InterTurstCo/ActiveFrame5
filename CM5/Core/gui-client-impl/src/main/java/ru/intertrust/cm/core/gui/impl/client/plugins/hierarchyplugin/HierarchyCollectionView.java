package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;


import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyCollectionConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyGroupConfig;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.ExpandHierarchyEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.ExpandHierarchyEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.HierarchyActionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.HierarchyActionEventHandler;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 29.07.2016
 * Time: 12:11
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyCollectionView extends HierarchyNode implements HierarchyActionEventHandler, ExpandHierarchyEventHandler {
    private HierarchyCollectionConfig collectionConfig;
    private Boolean expandable = false;
    private CollectionRowItem rowItem;

    public HierarchyCollectionView(HierarchyCollectionConfig aCollectionConfig, CollectionRowItem aRow) {
        rowItem = aRow;
        collectionConfig = aCollectionConfig;
        guiElementsFactory = new HierarchyGuiElementsFactory();
        guiFactory = new HierarchyGuiFactory();
        localBus = new SimpleEventBus();
        rootPanel = new AbsolutePanel();
        rootPanel.addStyleName(HierarchyPluginStaticData.STYLE_HEADER_CELL);
        headerPanel = new HorizontalPanel();
        headerPanel.addStyleName(HierarchyPluginStaticData.STYLE_WRAP_CELL);
        childPanel = new VerticalPanel();
        childPanel.addStyleName(HierarchyPluginStaticData.STYLE_CHILD_CELL);

        if(collectionConfig.getHierarchyGroupConfigs().size()>0  ||
                collectionConfig.getHierarchyCollectionConfigs().size()>0){
            expandable = true;
        }

        addRepresentationCells(headerPanel);
        rootPanel.add(headerPanel);
        rootPanel.add(childPanel);
        childPanel.setVisible(expanded);
        initWidget(rootPanel);

        localBus.addHandler(ExpandHierarchyEvent.TYPE, this);
        localBus.addHandler(HierarchyActionEvent.TYPE, this);
    }

    @Override
    protected void addRepresentationCells(Panel container) {

        FlexTable grid = new FlexTable();
        grid.addStyleName(HierarchyPluginStaticData.STYLE_REPRESENTATION_CELL);
        if (expandable) {
            grid.setWidget(0, 0, guiElementsFactory.buildExpandCell(localBus));
        }

        int columnIndex = 1;
        for(String key : rowItem.getRow().keySet()){
            InlineHTML fieldName = new InlineHTML("<b>" + key + "</b>");
            fieldName.addStyleName(HierarchyPluginStaticData.STYLE_FIELD_NAME);
            grid.setWidget(0, columnIndex, fieldName);
            InlineHTML fieldValue = new InlineHTML(rowItem.getRow().get(key).toString());
            fieldValue.addStyleName(HierarchyPluginStaticData.STYLE_FIELD_VALUE);
            grid.setWidget(0, columnIndex+1, fieldValue);
            columnIndex+=2;
        }

        container.add(grid);
    }

    @Override
    public void onExpandHierarchyEvent(ExpandHierarchyEvent event) {
        expanded = event.isExpand();

        if (expanded) {
            for (HierarchyGroupConfig group : collectionConfig.getHierarchyGroupConfigs()) {
                childPanel.add(guiFactory.buildGroup(group));
            }
            for (HierarchyCollectionConfig collection : collectionConfig.getHierarchyCollectionConfigs()) {
                childPanel.add(guiFactory.buildCollection(collection));
            }
        } else {
            childPanel.clear();
        }


        childPanel.setVisible(expanded);
    }

    @Override
    public void onHierarchyActionEvent(HierarchyActionEvent event) {

    }
}
