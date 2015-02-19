package ru.intertrust.cm.core.gui.api.client;

import com.google.gwt.event.shared.GwtEvent;

public class DigitalSignatureComponentInitEvent extends GwtEvent<DigitalSignatureComponentInitHandler> { 
    private static final Type<DigitalSignatureComponentInitHandler> TYPE = new Type<DigitalSignatureComponentInitHandler>();
    
    @Override
    public GwtEvent.Type<DigitalSignatureComponentInitHandler> getAssociatedType() {
        return TYPE;
    }
    @Override
    protected void dispatch(DigitalSignatureComponentInitHandler handler) {
        handler.onInit();
    }
}
