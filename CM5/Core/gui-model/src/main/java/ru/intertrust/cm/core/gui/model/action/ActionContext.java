package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 13:21
 */
public class ActionContext implements Dto {
    private Id rootObjectId;

    public Id getRootObjectId() {
        return rootObjectId;
    }

    public void setRootObjectId(Id rootObjectId) {
        this.rootObjectId = rootObjectId;
    }
}
