package ru.intertrust.cm.core.gui.impl.client.form.widget.tooltip;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.web.bindery.event.shared.EventBus;
import ru.intertrust.cm.core.gui.impl.client.event.tooltip.ShowTooltipEvent;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.10.2014
 *         Time: 7:27
 */
public class TooltipButtonClickHandler implements ClickHandler {
    private EventBus eventBus;

    public TooltipButtonClickHandler(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public void onClick(ClickEvent event) {
        eventBus.fireEvent(new ShowTooltipEvent());
    }
}
