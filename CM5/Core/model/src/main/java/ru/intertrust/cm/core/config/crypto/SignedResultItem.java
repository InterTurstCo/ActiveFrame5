package ru.intertrust.cm.core.config.crypto;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

public class SignedResultItem implements Dto{
    private static final long serialVersionUID = 1769779018831838602L;
    
    private Id id;
    private String signature;

    public SignedResultItem() {
    }
    
    public SignedResultItem(Id id, String signature) {
        super();
        this.id = id;
        this.signature = signature;
    }
    public Id getId() {
        return id;
    }
    public void setId(Id id) {
        this.id = id;
    }
    public String getSignature() {
        return signature;
    }
    public void setSignature(String signature) {
        this.signature = signature;
    }
}
