package ru.intertrust.cm.globalcacheclient;

import ru.intertrust.cm.globalcache.api.GroupAccessChanges;
import ru.intertrust.cm.globalcache.api.PersonAccessChanges;

/**
 * @author Denis Mitavskiy
 *         Date: 05.08.2015
 *         Time: 19:27
 */
public interface PersonAccessHelper {
    PersonAccessChanges getPersonAccessChanges(GroupAccessChanges groupAccessChanges);
}
