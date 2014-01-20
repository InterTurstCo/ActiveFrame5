package ru.intertrust.cm.core.gui.impl.client.event;


import com.google.gwt.event.shared.GwtEvent;

/**
 * Created with IntelliJ IDEA.
 * User: lvov
 * Date: 18.01.14
 * Time: 17:01
 * To change this template use File | Settings | File Templates.
 */
public class SaveToCsvEvent extends GwtEvent<SaveToCsvEventHandler> {
    public static final Type<SaveToCsvEventHandler> TYPE = new Type<SaveToCsvEventHandler>();

    @Override
    public Type<SaveToCsvEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(SaveToCsvEventHandler handler) {
        handler.saveToCsv(this);
    }
}
