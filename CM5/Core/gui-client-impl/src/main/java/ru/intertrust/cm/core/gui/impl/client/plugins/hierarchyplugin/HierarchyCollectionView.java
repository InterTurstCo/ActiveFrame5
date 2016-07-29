package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;


import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyCollectionConfig;
import ru.intertrust.cm.core.gui.model.plugin.collection.CollectionRowItem;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 29.07.2016
 * Time: 12:11
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyCollectionView extends HierarchyNode {
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
        //rootPanel.addStyleName(HierarchyPluginStaticData.STYLE_PARENT_PANEL);
        headerPanel = new HorizontalPanel();
        //headerPanel.addStyleName(HierarchyPluginStaticData.STYLE_HEADER_PANEL);
        childPanel = new VerticalPanel();
        //childPanel.addStyleName(HierarchyPluginStaticData.STYLE_CHILD_PANEL);
        addRepresentationCells(headerPanel);
        rootPanel.add(headerPanel);
        rootPanel.add(childPanel);
        childPanel.setVisible(expanded);
        initWidget(rootPanel);
    }

    @Override
    protected void addRepresentationCells(Panel container) {
        FlexTable grid = new FlexTable();
        FlexTable.FlexCellFormatter cellFormatter = grid.getFlexCellFormatter();
        //grid.addStyleName(HierarchyPluginStaticData.STYLE_WRAP_PANEL);
        if (expandable) {
            grid.setWidget(0, 0, guiElementsFactory.buildExpandCell(localBus));
        }

        int columnIndex = 1;
        for(String key : rowItem.getRow().keySet()){
            InlineHTML fieldName = new InlineHTML("<b>" + key + "</b>");
            grid.setWidget(0, columnIndex, fieldName);
            InlineHTML fieldValue = new InlineHTML(rowItem.getRow().get(key).toString());
            grid.setWidget(0, columnIndex+1, fieldValue);
            columnIndex+=2;
        }




        //cellFormatter.setStyleName(0, 1, HierarchyPluginStaticData.STYLE_GROUP_NAME);
        container.add(grid);
    }
}
