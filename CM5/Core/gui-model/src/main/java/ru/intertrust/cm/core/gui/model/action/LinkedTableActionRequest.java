package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
* Created by andrey on 21.12.14.
*/
public class LinkedTableActionRequest implements Dto {

    private String accessCheckerComponent;
    private String newObjectsAccessCheckerComponent;
    private Id objectId;
    private int rowIndex;

    public void setAccessCheckerComponent(String accessCheckerComponent) {
        this.accessCheckerComponent = accessCheckerComponent;
    }

    public String getAccessCheckerComponent() {
        return accessCheckerComponent;
    }

    public void setNewObjectsAccessCheckerComponent(String newObjectsAccessCheckerComponent) {
        this.newObjectsAccessCheckerComponent = newObjectsAccessCheckerComponent;
    }

    public String getNewObjectsAccessCheckerComponent() {
        return newObjectsAccessCheckerComponent;
    }

    public void setObjectId(Id objectId) {
        this.objectId = objectId;
    }

    public Id getObjectId() {
        return objectId;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }
}
