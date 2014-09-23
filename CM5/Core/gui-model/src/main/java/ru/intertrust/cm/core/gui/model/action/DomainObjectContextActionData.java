package ru.intertrust.cm.core.gui.model.action;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * @author Sergey.Okolot
 *         Created on 23.09.2014 14:08.
 */
public abstract class DomainObjectContextActionData extends ActionData {

    public abstract DomainObject getContextDomainObject();
}
