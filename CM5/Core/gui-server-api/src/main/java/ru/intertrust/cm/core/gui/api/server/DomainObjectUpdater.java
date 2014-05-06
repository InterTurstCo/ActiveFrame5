package ru.intertrust.cm.core.gui.api.server;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * @author Yaroslav Bondacrhuk
 *         Date: 13/4/14
 *         Time: 12:05 PM
 */
public interface DomainObjectUpdater extends ComponentHandler {
    public void updateDomainObject(DomainObject domainObject, Dto updaterContext);
}
