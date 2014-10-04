package ru.intertrust.cm.core.gui.impl.client.event.tooltip;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 01.10.2014
 *         Time: 6:35
 */
public class WidgetItemRemoveEvent extends GwtEvent<WidgetItemRemoveEventHandler> {
    public static final  Type<WidgetItemRemoveEventHandler> TYPE = new Type<WidgetItemRemoveEventHandler>();
    private Id id;
    private boolean tooltipContent;

    public WidgetItemRemoveEvent(Id id, boolean tooltipContent) {
        this.id = id;
        this.tooltipContent = tooltipContent;
    }

    @Override
    public Type<WidgetItemRemoveEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(WidgetItemRemoveEventHandler handler) {
        handler.onWidgetItemRemove(this);

    }

    public Id getId() {
        return id;
    }

    public boolean isTooltipContent() {
        return tooltipContent;
    }
}