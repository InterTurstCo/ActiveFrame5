package ru.intertrust.cm.core.gui.api.client;

import com.google.gwt.event.shared.EventHandler;

public interface DigitalSignatureComponentInitHandler extends EventHandler{
    void onInit();
    void onCancel();
    void onError(String message);
}
