package ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetItemsView;

import java.util.LinkedHashMap;
import java.util.Set;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 30.06.2014
 *         Time: 13:38
 */
public class EditableWidgetTooltip extends PopupPanel{
    private EventBus eventBus;
    private WidgetItemsView widgetItemsView;
    private boolean displayAsHyperlinks;
    private Set<Id> selectedIds;
    public EditableWidgetTooltip(SelectionStyleConfig selectionStyleConfig, EventBus eventBus, boolean displayAsHyperlinks,
                                 Set<Id> selectedIds) {
        super(true);
        this.eventBus = eventBus;
        this.displayAsHyperlinks = displayAsHyperlinks;
        this.selectedIds = selectedIds;
        init(selectionStyleConfig);
    }
    private void init(SelectionStyleConfig selectionStyleConfig){
        widgetItemsView = new WidgetItemsView(selectionStyleConfig);
        widgetItemsView.setSelectedIds(selectedIds);
        widgetItemsView.setEventBus(eventBus);
        this.add(widgetItemsView);
        this.setStyleName("tooltip-popup");

    }
    public void displayItems(LinkedHashMap<Id, String> listValues) {
        widgetItemsView.setListValues(listValues);
        if(displayAsHyperlinks) {
            widgetItemsView.displayHyperlinkItems();
        } else {
            widgetItemsView.displayItems();
        }

    }

}
