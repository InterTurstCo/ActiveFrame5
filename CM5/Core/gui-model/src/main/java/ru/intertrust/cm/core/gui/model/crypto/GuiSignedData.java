package ru.intertrust.cm.core.gui.model.crypto;

import java.util.ArrayList;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

public class GuiSignedData implements Dto{
    private static final long serialVersionUID = 1094911161784917225L;

    private ArrayList<GuiSignedDataItem> signedDataItems = new ArrayList<GuiSignedDataItem>();
    
    private Id rootId;

    public Id getRootId() {
        return rootId;
    }

    public void setRootId(Id rootId) {
        this.rootId = rootId;
    }

    public ArrayList<GuiSignedDataItem> getSignedDataItems() {
        return signedDataItems;
    }

    public void setSignedDataItems(ArrayList<GuiSignedDataItem> signedDataItems) {
        this.signedDataItems = signedDataItems;
    }
}
