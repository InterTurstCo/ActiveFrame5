package ru.intertrust.cm.core.config.crypto;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

public class SignedResultItem implements Dto{
    private Id id;
    private String signature;
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
