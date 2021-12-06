package ru.intertrust.cm.core.gui.api.server.action;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.gui.action.ActionConfig;

/**
 * @author Sergey.Okolot
 *         Created on 01.09.2014 17:05.
 */
public class ActionVisibilityContext {

    private DomainObject domainObject;
    private ActionConfig actionConfig;

    public DomainObject getDomainObject() {
        return domainObject;
    }

    public ActionVisibilityContext setDomainObject(DomainObject domainObject) {
        this.domainObject = domainObject;
        return this;
    }

    public ActionConfig getActionConfig() {
        return actionConfig;
    }

    public void setActionConfig(ActionConfig actionConfig) {
        this.actionConfig = actionConfig;
    }
}
