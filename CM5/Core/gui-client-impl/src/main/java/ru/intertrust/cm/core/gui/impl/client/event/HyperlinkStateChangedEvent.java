package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.ui.PopupPanel;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 03.01.14
 *         Time: 13:15
 */
public class HyperlinkStateChangedEvent extends GwtEvent<HyperlinkStateChangedEventHandler> {

    public static Type<HyperlinkStateChangedEventHandler> TYPE = new Type<HyperlinkStateChangedEventHandler>();
    private Id id;
    private PopupPanel popupPanel;

    public HyperlinkStateChangedEvent(Id id, PopupPanel popupPanel) {
        this.id = id;
        this.popupPanel = popupPanel;
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

    public PopupPanel getPopupPanel() {
        return popupPanel;
    }
}
