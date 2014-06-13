package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 19.09.13
 *         Time: 13:21
 */
public class ActionContext implements Dto {
    private Id rootObjectId;

    private AbstractActionConfig actionConfig;

    /**
     * Default constructor.
     */
    public ActionContext(){}

    public ActionContext(final AbstractActionConfig actionConfig) {
        this.actionConfig = actionConfig;
    }

    public Id getRootObjectId() {
        return rootObjectId;
    }

    public void setRootObjectId(Id rootObjectId) {
        this.rootObjectId = rootObjectId;
    }

    public <T extends AbstractActionConfig> T getActionConfig() {
        return (T) actionConfig;
    }

    public void setActionConfig(AbstractActionConfig actionConfig) {
        this.actionConfig = actionConfig;
    }
}
