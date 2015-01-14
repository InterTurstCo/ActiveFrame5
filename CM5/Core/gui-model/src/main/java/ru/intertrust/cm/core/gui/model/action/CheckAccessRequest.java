package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Created by andrey on 29.12.14.
 */
public class CheckAccessRequest implements Dto {
    private String accessCheckerName;
    private Id objectId;

    public String getAccessCheckerName() {
        return accessCheckerName;
    }

    public void setAccessCheckerName(String accessCheckerName) {
        this.accessCheckerName = accessCheckerName;
    }

    public Id getObjectId() {
        return objectId;
    }

    public void setObjectId(Id objectId) {
        this.objectId = objectId;
    }
}
