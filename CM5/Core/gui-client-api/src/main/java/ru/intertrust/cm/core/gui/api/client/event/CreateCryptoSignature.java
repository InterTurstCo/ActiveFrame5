package ru.intertrust.cm.core.gui.api.client.event;

import com.google.gwt.event.shared.GwtEvent;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.crypto.SignedResult;

public class CreateCryptoSignature extends GwtEvent<CreateCryptoSignatureHandler> {
    public static final Type<CreateCryptoSignatureHandler> TYPE = new Type<CreateCryptoSignatureHandler>();

    private SignedResult signResult;
    private String errorMessage;
    private Id rootId;

    public CreateCryptoSignature(SignedResult signResult) {
        this.signResult = signResult;
    }

    public CreateCryptoSignature(Id rootId, String errorMessage) {
        this.errorMessage = errorMessage;
        this.rootId = rootId;
    }    
    
    @Override
    public com.google.gwt.event.shared.GwtEvent.Type<CreateCryptoSignatureHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CreateCryptoSignatureHandler handler) {
        if (signResult != null){
            handler.onCreateCryptoSignature(signResult);
        }else{
            handler.onErrorCreateCryptoSignature(rootId, errorMessage);
        }
    }
}
