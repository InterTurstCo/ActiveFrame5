package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Lesia Puhova
 *         Date: 17.10.14
 *         Time: 16:31
 */
public class UploadCompletedEvent extends GwtEvent<UploadCompletedEventHandler> {

    public static final Type<UploadCompletedEventHandler> TYPE = new Type<>();

    @Override
    public Type<UploadCompletedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(UploadCompletedEventHandler handler) {
        handler.onUploadCompleted(this);
    }
}
