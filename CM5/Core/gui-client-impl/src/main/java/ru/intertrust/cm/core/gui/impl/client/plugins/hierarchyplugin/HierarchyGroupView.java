package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyGroupConfig;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.ExpandHierarchyEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.ExpandHierarchyEventHandler;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 27.07.2016
 * Time: 10:09
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyGroupView extends Composite implements ExpandHierarchyEventHandler {

    private EventBus localBus;
    private HierarchyGroupConfig groupConfig;
    private AbsolutePanel rootPanel;
    private HorizontalPanel headerPanel;
    private HorizontalPanel childPanel;
    private HierarchyGuiElementsFactory guiElementsFactory;
    private HierarchyGuiFactory guiFactory;
    private Boolean expanded = false;

    public HierarchyGroupView(HierarchyGroupConfig aGroupConfig) {
        groupConfig = aGroupConfig;
        guiElementsFactory = new HierarchyGuiElementsFactory();
        guiFactory = new HierarchyGuiFactory();
        localBus = new SimpleEventBus();
        rootPanel = new AbsolutePanel();
        headerPanel = new HorizontalPanel();
        childPanel = new HorizontalPanel();
        addRepresentationCells(headerPanel);
        rootPanel.add(headerPanel);
        rootPanel.add(childPanel);
        childPanel.setVisible(expanded);
        initWidget(rootPanel);

        localBus.addHandler(ExpandHierarchyEvent.TYPE, this);
    }

    protected void addRepresentationCells(Panel container) {
        //tableRoot.addStyleName(CoordinationUtil.STYLE_ROW_DETAILS_WRAPPER);
        FlexTable grid = new FlexTable();

        //grid.addStyleName(CoordinationUtil.STYLE_ROW_DETAILS_GRID);
        //grid.addStyleName(CoordinationUtil.STYLE_ROW_GRID);

        grid.setWidget(0, 0, guiElementsFactory.buildExpandCell(localBus));

        InlineHTML groupName = new InlineHTML("<b>" + groupConfig.getName() + "</b>");
        grid.setWidget(0, 1, groupName);


        MenuBar actionsMenu = new MenuBar();
        MenuBar dropDownMenu = new MenuBar(true);
        //actionsMenu.setStyleName("buttonApprovalEdit");
        //fooMenu.setStyleName("wrapApproval");

        dropDownMenu.addItem(new MenuItem("Обновить", guiElementsFactory.getACommand()));
        dropDownMenu.addItem(new MenuItem("Сортировать", guiElementsFactory.getACommand()));
        dropDownMenu.addItem(new MenuItem("Добавить", guiElementsFactory.getACommand()));

        actionsMenu.addItem("Действия", dropDownMenu);
        grid.setWidget(0, 2, actionsMenu);
        container.add(grid);
    }


    @Override
    public void onExpandHierarchyEvent(ExpandHierarchyEvent event) {
        expanded = event.isExpand();
        if (expanded) {
            for(HierarchyGroupConfig group : groupConfig.getHierarchyGroupConfigs()){
                childPanel.add(guiFactory.buildGroup(group));
            }

        } else {
            childPanel.clear();
        }
        childPanel.setVisible(expanded);
    }
}
