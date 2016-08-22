package ru.intertrust.cm.core.gui.api.server;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 04.06.2015
 *         Time: 9:07
 */
public interface UserSettingsFetcher {

    public interface Remote extends UserSettingsFetcher{
    }

    DomainObject getUserSettingsDomainObject(boolean lock);

    DomainObject getUserHipSettingsDomainObject(String pId);
}
