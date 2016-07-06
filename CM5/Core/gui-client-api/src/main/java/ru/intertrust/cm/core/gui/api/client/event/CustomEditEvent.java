package ru.intertrust.cm.core.gui.api.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created by IntelliJ IDEA.
 * Developer: Ravil Abdulkhairov
 * Date: 06.07.2016
 * Time: 9:57
 * To change this template use File | Settings | File and Code Templates.
 */
public class CustomEditEvent extends GwtEvent<CustomEditEventHandler> {
    public  static final Type<CustomEditEventHandler> TYPE = new Type<CustomEditEventHandler>();

    @Override
    public Type<CustomEditEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CustomEditEventHandler customEditEventHandler) {
        customEditEventHandler.onEdit(this);
    }
}
