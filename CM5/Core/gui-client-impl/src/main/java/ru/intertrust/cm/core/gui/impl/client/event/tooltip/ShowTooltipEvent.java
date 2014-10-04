package ru.intertrust.cm.core.gui.impl.client.event.tooltip;

import com.google.gwt.event.shared.GwtEvent;


/**
 * @author Yaroslav Bondarchuk
 *         Date: 02.10.2014
 *         Time: 7:16
 */
public class ShowTooltipEvent extends GwtEvent<ShowTooltipEventHandler> {
    public static final Type<ShowTooltipEventHandler> TYPE = new Type<ShowTooltipEventHandler>();

    public ShowTooltipEvent() {

    }

    @Override
    public Type<ShowTooltipEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(ShowTooltipEventHandler handler) {
        handler.showTooltip(this);

    }
}
