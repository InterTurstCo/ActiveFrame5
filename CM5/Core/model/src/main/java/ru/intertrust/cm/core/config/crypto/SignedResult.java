package ru.intertrust.cm.core.config.crypto;

import java.util.ArrayList;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

public class SignedResult implements Dto{
    private static final long serialVersionUID = 1094911185684917225L;

    private ArrayList<SignedResultItem> signedResultItems = new ArrayList<SignedResultItem>();
    
    private Id rootId;


    public Id getRootId() {
        return rootId;
    }

    public void setRootId(Id rootId) {
        this.rootId = rootId;
    }

    public ArrayList<SignedResultItem> getSignedResultItems() {
        return signedResultItems;
    }

    public void setSignedResultItems(ArrayList<SignedResultItem> signedResultItems) {
        this.signedResultItems = signedResultItems;
    }
}
