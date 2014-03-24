package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by lvov on 24.03.14.
 */
public class PopupGlassEvent extends GwtEvent<PopupGlassEventHandler> {
    public static final Type<PopupGlassEventHandler> TYPE = new Type<PopupGlassEventHandler>();

    @Override
    public Type<PopupGlassEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PopupGlassEventHandler handler) {
        handler.hidePopupGlass(this);

    }
}
