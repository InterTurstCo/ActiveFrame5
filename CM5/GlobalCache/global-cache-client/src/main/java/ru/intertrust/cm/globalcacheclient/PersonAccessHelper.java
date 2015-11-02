package ru.intertrust.cm.globalcacheclient;

import ru.intertrust.cm.globalcache.api.GroupAccessChanges;
import ru.intertrust.cm.globalcache.api.PersonAccessChanges;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.HashSet;

/**
 * @author Denis Mitavskiy
 *         Date: 05.08.2015
 *         Time: 19:27
 */
public interface PersonAccessHelper {
    PersonAccessChanges getPersonAccessChanges(GroupAccessChanges groupAccessChanges);

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    PersonAccessChanges getPersonAccessChanges(GroupAccessChanges groupAccessChanges, HashSet<String> objectTypesAccessChanged);
}
