package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.impl.client.form.widget.hyperlink.HyperlinkDisplay;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class HyperlinkStateChangedEvent extends GwtEvent<HyperlinkStateChangedEventHandler> {

    public static Type<HyperlinkStateChangedEventHandler> TYPE = new Type<HyperlinkStateChangedEventHandler>();
    private Id id;
    private HyperlinkDisplay hyperlinkDisplay;
    private boolean tooltipContent;

    public HyperlinkStateChangedEvent(Id id, HyperlinkDisplay hyperlinkDisplay, boolean tooltipContent) {
        this.id = id;
        this.hyperlinkDisplay = hyperlinkDisplay;
        this.tooltipContent = tooltipContent;
    }

    @Override
    public Type<HyperlinkStateChangedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(HyperlinkStateChangedEventHandler handler) {
        handler.onHyperlinkStateChangedEvent(this);
    }

    public Id getId() {
        return id;
    }

    public HyperlinkDisplay getHyperlinkDisplay() {
        return hyperlinkDisplay;
    }

    public boolean isTooltipContent() {
        return tooltipContent;
    }
}
