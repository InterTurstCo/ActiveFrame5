package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Карта делегирования прав доступа к объекту. Если в этой карте отсутствует Id какого-то объекта, это означает, что
 * права доступа к объекту определяются им самим (если он, конечно, находится в кэше).
 * @author Denis Mitavskiy
 *         Date: 24.07.2015
 *         Time: 19:50
 */
public class ObjectAccessDelegation {
    private ConcurrentMap<Id, Id> delegateById;
    private ConcurrentMap<Id, Set<Id>> objectsByDelegate; // key - object, which defines user access. value - objects delegating their checks to the key

    public ObjectAccessDelegation(int concurrencyLevel) {
        delegateById = new ConcurrentHashMap<>(concurrencyLevel);
        objectsByDelegate = new ConcurrentHashMap<>(concurrencyLevel);
    }

    public void setDelegation(Id objectId, Id accessCheckDelegateId) {
        if (!objectId.equals(accessCheckDelegateId)) {
            delegateById.put(objectId, accessCheckDelegateId);
            findOrCreateObjectsByDelegate(accessCheckDelegateId).add(objectId);
        }
    }

    public void removeId(Id objectId) {
        final Set<Id> dependentObjects = objectsByDelegate.remove(objectId);
        if (dependentObjects != null) { // object is a delegate, so it's not a depending object, no entries in delegateById
            return;
        }

        final Id delegate = delegateById.remove(objectId);
        if (delegate == null) {
            return;
        }
        // object has a delegate, so remove it from it's list
        final Set<Id> objectsDelegateResponsibleFor = objectsByDelegate.get(delegate);
        if (objectsDelegateResponsibleFor != null) {
            objectsDelegateResponsibleFor.remove(objectId);
        }
    }

    public Id getDelegate(Id id) {
        return delegateById.get(id);
    }

    public Set<Id> getObjectsByDelegate(Id accessCheckDelegateId) {
        return objectsByDelegate.get(accessCheckDelegateId);
    }

    private Set<Id> findOrCreateObjectsByDelegate(Id accessCheckDelegateId) {
        Set<Id> objectsForDelegate = objectsByDelegate.get(accessCheckDelegateId);
        if (objectsForDelegate == null) {
            objectsForDelegate = Collections.synchronizedSet(new HashSet<Id>());
            Set<Id> previous = objectsByDelegate.putIfAbsent(accessCheckDelegateId, objectsForDelegate);
            if (previous != null) {
                objectsForDelegate = previous;
            }
        }
        return objectsForDelegate;
    }
}
