package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyCollectionConfig;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyGroupConfig;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.ExpandHierarchyEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.ExpandHierarchyEventHandler;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.HierarchyActionEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.HierarchyActionEventHandler;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 27.07.2016
 * Time: 10:09
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyGroupView extends HierarchyNode
        implements HierarchyActionEventHandler,ExpandHierarchyEventHandler {


    private HierarchyGroupConfig groupConfig;
    public HierarchyGroupView(HierarchyGroupConfig aGroupConfig) {
        groupConfig = aGroupConfig;
        guiElementsFactory = new HierarchyGuiElementsFactory();
        guiFactory = new HierarchyGuiFactory();
        localBus = new SimpleEventBus();
        rootPanel = new AbsolutePanel();
        rootPanel.addStyleName(HierarchyPluginStaticData.STYLE_PARENT_PANEL);
        headerPanel = new HorizontalPanel();
        headerPanel.addStyleName(HierarchyPluginStaticData.STYLE_HEADER_PANEL);
        childPanel = new VerticalPanel();
        childPanel.addStyleName(HierarchyPluginStaticData.STYLE_CHILD_PANEL);
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
        FlexTable.FlexCellFormatter cellFormatter = grid.getFlexCellFormatter();
        grid.addStyleName(HierarchyPluginStaticData.STYLE_WRAP_PANEL);

        grid.setWidget(0, 0, guiElementsFactory.buildExpandCell(localBus));

        InlineHTML groupName = new InlineHTML("<b>" + groupConfig.getName() + "</b>");
        grid.setWidget(0, 1, groupName);


        grid.setWidget(0, 2, guiElementsFactory.buildActionButton(localBus, HierarchyPluginStaticData.Actions.GROUPREFRESH));
        grid.setWidget(0, 3, guiElementsFactory.buildActionButton(localBus, HierarchyPluginStaticData.Actions.GROUPSORT));
        grid.setWidget(0, 4, guiElementsFactory.buildActionButton(localBus, HierarchyPluginStaticData.Actions.GROUPADD));
        cellFormatter.setStyleName(0, 1, HierarchyPluginStaticData.STYLE_GROUP_NAME);
        container.add(grid);
    }


    @Override
    public void onExpandHierarchyEvent(ExpandHierarchyEvent event) {
        expanded = event.isExpand();

        if (expanded) {
            for (HierarchyGroupConfig group : groupConfig.getHierarchyGroupConfigs()) {
                childPanel.add(guiFactory.buildGroup(group));
            }
            for (HierarchyCollectionConfig collection : groupConfig.getHierarchyCollectionConfigs()) {
                childPanel.add(guiFactory.buildCollection(collection));
            }
        } else {
            childPanel.clear();
        }


        childPanel.setVisible(expanded);
    }

    @Override
    public void onHierarchyActionEvent(HierarchyActionEvent event) {
        Window.alert("Действие: " + event.getAction().toString());
    }

}
