package ru.intertrust.cm.core.business.api.dto.crypto;

import ru.intertrust.cm.core.business.api.dto.Id;

public class DocumentVerifyResult extends VerifyResult{

    private static final long serialVersionUID = 322499632764799861L;
    
    private Id documentId;

    private String documentName;
    
    public Id getDocumentId() {
        return documentId;
    }
    public void setDocumentId(Id documentId) {
        this.documentId = documentId;
    }
    public String getDocumentName() {
        return documentName;
    }
    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

}
