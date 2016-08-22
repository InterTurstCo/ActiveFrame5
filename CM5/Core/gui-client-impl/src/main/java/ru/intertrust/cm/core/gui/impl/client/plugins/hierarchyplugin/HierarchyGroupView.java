package ru.intertrust.cm.core.gui.impl.client.plugins.hierarchyplugin;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.hierarchyplugin.HierarchyGroupConfig;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchyplugin.*;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 27.07.2016
 * Time: 10:09
 * To change this template use File | Settings | File and Code Templates.
 */
public class HierarchyGroupView extends HierarchyNode
        implements HierarchyActionEventHandler {


    public HierarchyGroupView(HierarchyGroupConfig aGroupConfig, Id aParentId, EventBus aCommonBus, String aParentViewId) {
        super();
        commonBus = aCommonBus;
        parentId = aParentId;
        groupConfig = aGroupConfig;
        rootPanel.addStyleName(STYLE_PARENT_PANEL);
        headerPanel.addStyleName(STYLE_HEADER_PANEL);
        childPanel.addStyleName(STYLE_CHILD_PANEL);
        setViewID(aGroupConfig.getGid() + ((parentId != null) ? "-" + parentId.toStringRepresentation() : ""));
        parentViewID = aParentViewId;
        addRepresentationCells(headerPanel);
        rootPanel.add(headerPanel);
        rootPanel.add(childPanel);
        childPanel.setVisible(expanded);
        initWidget(rootPanel);
        commonBus.fireEvent(new NodeCreatedEvent(getViewID()));
        commonBus.addHandler(AutoOpenEvent.TYPE, this);
        localBus.addHandler(ExpandHierarchyEvent.TYPE, this);
        localBus.addHandler(HierarchyActionEvent.TYPE, this);

    }

    @Override
    protected void addRepresentationCells(Panel container) {
        FlexTable grid = new FlexTable();
        FlexTable.FlexCellFormatter cellFormatter = grid.getFlexCellFormatter();
        grid.addStyleName(STYLE_WRAP_PANEL);

        grid.setWidget(0, 0, guiElementsFactory.buildExpandCell(commonBus, localBus, parentId, getViewID(), getParentViewID()));
        expandButton = grid.getWidget(0, 0);

        InlineHTML groupName = new InlineHTML("<b>" + groupConfig.getName() + "</b>");
        grid.setWidget(0, 1, groupName);


        grid.setWidget(0, 2, guiElementsFactory.buildActionButton(localBus, Actions.GROUPREFRESH));
        grid.setWidget(0, 3, guiElementsFactory.buildActionButton(localBus, Actions.GROUPSORT));
        grid.setWidget(0, 4, guiElementsFactory.buildActionButton(localBus, Actions.GROUPADD));
        cellFormatter.setStyleName(0, 1, STYLE_GROUP_NAME);
        container.add(grid);
    }


    @Override
    public void onHierarchyActionEvent(HierarchyActionEvent event) {
        Window.alert("Действие: " + event.getAction().toString());
    }

}
