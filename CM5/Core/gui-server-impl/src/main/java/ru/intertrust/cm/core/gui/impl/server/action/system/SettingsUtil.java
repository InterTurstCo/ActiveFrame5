package ru.intertrust.cm.core.gui.impl.server.action.system;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AccessToken;

import java.util.List;

/**
 * Created by Ravil on 16.01.2018.
 */
public interface SettingsUtil {
    void deleteIds(List<Id> ids);
    void deleteIds(List<Id> ids, AccessToken token);
    void saveTheme(String theme);
    DomainObject createNewObject(String link, Id person,Long count,String vName);
    void saveCounter(Id id,Long counter);
}
