package ru.intertrust.cm.core.gui.impl.client.form.widget.hierarchybrowser;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserCheckBoxUpdateEvent;
import ru.intertrust.cm.core.gui.impl.client.event.hierarchybrowser.HierarchyBrowserCheckBoxUpdateEventHandler;
import ru.intertrust.cm.core.gui.model.form.widget.HierarchyBrowserItem;

import java.util.ArrayList;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 08.07.2014
 *         Time: 21:02
 */
public class HierarchyBrowserEditableTooltip extends PopupPanel{
    private EventBus eventBus;
    private HierarchyBrowserItemsView widgetItemsView;
    private boolean displayAsHyperlinks;
    public HierarchyBrowserEditableTooltip(SelectionStyleConfig selectionStyleConfig, EventBus eventBus,
                                           boolean displayAsHyperlinks) {
        super(true);
        this.eventBus = eventBus;
        this.displayAsHyperlinks = displayAsHyperlinks;
        init(selectionStyleConfig);
        eventBus.addHandler(HierarchyBrowserCheckBoxUpdateEvent.TYPE, new HierarchyBrowserCheckBoxUpdateEventHandler() {
            @Override
            public void onHierarchyBrowserCheckBoxUpdate(HierarchyBrowserCheckBoxUpdateEvent event) {
                if(widgetItemsView.isEmpty()){
                    HierarchyBrowserEditableTooltip.this.hide();
                }
            }
        });
    }

    private void init(SelectionStyleConfig selectionStyleConfig) {
        widgetItemsView = new HierarchyBrowserItemsView(selectionStyleConfig, eventBus, displayAsHyperlinks);
        widgetItemsView.setTooltipContent(true);
        this.add(widgetItemsView);
        this.setStyleName("tooltipPopup");

    }

    public void displayItems(ArrayList<HierarchyBrowserItem> items) {
        widgetItemsView.displayChosenItems(items, false);

    }

}
