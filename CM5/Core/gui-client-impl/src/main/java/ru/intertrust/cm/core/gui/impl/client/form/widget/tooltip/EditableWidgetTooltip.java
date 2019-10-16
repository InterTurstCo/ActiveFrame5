package ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.form.PopupTitlesHolder;
import ru.intertrust.cm.core.config.gui.form.widget.HasLinkedFormMappings;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.WidgetItemRemoveEvent;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.WidgetItemRemoveEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetItemsView;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 30.06.2014
 *         Time: 13:38
 */
public class EditableWidgetTooltip extends PopupPanel {
    private EventBus eventBus;
    private WidgetItemsView widgetItemsView;
    private boolean displayAsHyperlinks;
    private HasLinkedFormMappings hasLinkedFormMappings;

    public EditableWidgetTooltip(SelectionStyleConfig selectionStyleConfig, EventBus eventBus,
            boolean displayAsHyperlinks, Map<String, PopupTitlesHolder> typeTitleMap, HasLinkedFormMappings hasLinkedFormMappings) {

        super(true);
        this.eventBus = eventBus;
        this.displayAsHyperlinks = displayAsHyperlinks;
        this.hasLinkedFormMappings = hasLinkedFormMappings;
        init(selectionStyleConfig, typeTitleMap);
        eventBus.addHandler(WidgetItemRemoveEvent.TYPE, new WidgetItemRemoveEventHandler() {
            @Override
            public void onWidgetItemRemove(WidgetItemRemoveEvent event) {
                if(widgetItemsView.isEmpty()){
                    EditableWidgetTooltip.this.hide();
                }
            }
        });
    }

    private void init(SelectionStyleConfig selectionStyleConfig, Map<String, PopupTitlesHolder> typeTitleMap) {
        widgetItemsView = new WidgetItemsView(selectionStyleConfig, typeTitleMap, hasLinkedFormMappings, eventBus);
        this.add(widgetItemsView);
        this.setStyleName("tooltipPopup");

    }

    public void displayItems(LinkedHashMap<Id, String> listValues) {

        if (displayAsHyperlinks) {
            widgetItemsView.displayHyperlinks(listValues, false);
        } else {
            widgetItemsView.displayItems(listValues, false);
        }

    }

}
