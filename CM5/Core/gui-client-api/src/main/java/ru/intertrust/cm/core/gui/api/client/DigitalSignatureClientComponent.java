package ru.intertrust.cm.core.gui.api.client;

import ru.intertrust.cm.core.gui.model.crypto.DigitalSignatureConfig;

public abstract class DigitalSignatureClientComponent extends BaseComponent{
    public abstract String sign(String base64Content);
    
    public abstract void init(DigitalSignatureConfig config, DigitalSignatureComponentInitHandler onInit);
}
