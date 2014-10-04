package ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.widget.SelectionStyleConfig;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.WidgetItemRemoveEvent;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.WidgetItemRemoveEventHandler;
import ru.intertrust.cm.core.gui.impl.client.form.WidgetItemsView;

import java.util.LinkedHashMap;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 30.06.2014
 *         Time: 13:38
 */
public class EditableWidgetTooltip extends PopupPanel {
    private EventBus eventBus;
    private WidgetItemsView widgetItemsView;
    private boolean displayAsHyperlinks;

    public EditableWidgetTooltip(SelectionStyleConfig selectionStyleConfig, EventBus eventBus
                                                                 ,boolean displayAsHyperlinks) {

        super(true);
        this.eventBus = eventBus;
        this.displayAsHyperlinks = displayAsHyperlinks;
        init(selectionStyleConfig);
        eventBus.addHandler(WidgetItemRemoveEvent.TYPE, new WidgetItemRemoveEventHandler() {
            @Override
            public void onWidgetItemRemove(WidgetItemRemoveEvent event) {
                if(widgetItemsView.isEmpty()){
                    EditableWidgetTooltip.this.hide();
                }
            }
        });
    }

    private void init(SelectionStyleConfig selectionStyleConfig) {
        widgetItemsView = new WidgetItemsView(selectionStyleConfig);
        widgetItemsView.setEventBus(eventBus);
        widgetItemsView.setTooltipContent(true);
        this.add(widgetItemsView);
        this.setStyleName("tooltip-popup");

    }

    public void displayItems(LinkedHashMap<Id, String> listValues) {

        if (displayAsHyperlinks) {
            widgetItemsView.displayHyperlinks(listValues, false);
        } else {
            widgetItemsView.displayItems(listValues, false);
        }

    }

}
