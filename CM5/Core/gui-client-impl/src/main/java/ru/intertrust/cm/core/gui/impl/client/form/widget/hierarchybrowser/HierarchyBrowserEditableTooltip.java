package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.07.2014
 *         Time: 21:02
 */
public class HierarchyBrowserEditableTooltip extends PopupPanel {
    private EventBus eventBus;
    private HierarchyBrowserItemsView widgetItemsView;
    private boolean displayAsHyperlinks;

    public HierarchyBrowserEditableTooltip(SelectionStyleConfig selectionStyleConfig, EventBus eventBus,
                                           boolean displayAsHyperlinks) {

        super(true);
        this.eventBus = eventBus;
        this.displayAsHyperlinks = displayAsHyperlinks;
        init(selectionStyleConfig);
    }

    private void init(SelectionStyleConfig selectionStyleConfig){
        widgetItemsView = new HierarchyBrowserItemsView(selectionStyleConfig, eventBus, displayAsHyperlinks);
        this.add(widgetItemsView);
        this.setStyleName("tooltip-popup");

    }

    public void displayItems(ArrayList<HierarchyBrowserItem> items, ArrayList<Id> selectedIds) {
        widgetItemsView.handleAddingChosenItems(items, selectedIds);

    }
}
