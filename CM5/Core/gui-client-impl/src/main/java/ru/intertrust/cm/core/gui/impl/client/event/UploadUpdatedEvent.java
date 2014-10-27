package ru.intertrust.cm.core.gui.impl.client.event;

import com.google.gwt.event.shared.GwtEvent;

/**
 * @author Lesia Puhova
 *         Date: 17.10.14
 *         Time: 13:51
 */
public class UploadUpdatedEvent extends GwtEvent<UploadUpdatedEventHandler> {

    public static final Type<UploadUpdatedEventHandler> TYPE = new Type<>();

    private Integer percentageValue;

    public UploadUpdatedEvent(Integer percentageValue) {
        this.percentageValue = percentageValue;
    }

    public Integer getPercentageValue() {
        return percentageValue;
    }

    @Override
    public Type<UploadUpdatedEventHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(UploadUpdatedEventHandler handler) {
        handler.onPercentageUpdated(this);
    }
}
