package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;
import ru.intertrust.cm.globalcache.api.util.SizeableConcurrentHashMap;

import java.util.Set;

/**
 * Карта делегирования прав доступа к объекту. Если в этой карте отсутствует Id какого-то объекта, это означает, что
 * права доступа к объекту определяются им самим (если он, конечно, находится в кэше).
 * @author Denis Mitavskiy
 *         Date: 24.07.2015
 *         Time: 19:50
 */
public class ObjectAccessDelegation implements Sizeable {
    private SizeableConcurrentHashMap<Id, Id> delegateById;
    private SizeableConcurrentHashMap<Id, SizeableConcurrentHashMap<Id, Id>> objectsByDelegate; // key - object, which defines user access. value - objects delegating their checks to the key

    private Size cacheTotalSize;

    public ObjectAccessDelegation(int concurrencyLevel, Size totalSize) {
        cacheTotalSize = totalSize;
        cacheTotalSize.add(2 * SizeEstimator.REFERENCE_SIZE);

        delegateById = new SizeableConcurrentHashMap<>(16, 0.75f, concurrencyLevel, cacheTotalSize, true, true);
        objectsByDelegate = new SizeableConcurrentHashMap<>(16, 0.75f, concurrencyLevel, cacheTotalSize, false, false);
    }

    public void setDelegation(Id objectId, Id accessCheckDelegateId) {
        if (!objectId.equals(accessCheckDelegateId)) {
            delegateById.put(objectId, accessCheckDelegateId);
            synchronized (objectsByDelegate) { // todo
                findOrCreateObjectsByDelegate(accessCheckDelegateId).put(objectId, objectId);
            }
        }
    }

    public void removeId(Id objectId) {
        final SizeableConcurrentHashMap<Id, Id> dependentObjects = objectsByDelegate.remove(objectId);
        if (dependentObjects != null) { // object is a delegate, so it's not a depending object, no entries in delegateById
            return;
        }

        final Id delegate = delegateById.remove(objectId);
        if (delegate == null) {
            return;
        }
        // object has a delegate, so remove it from it's list
        final SizeableConcurrentHashMap<Id, Id> objectsDelegateResponsibleFor = objectsByDelegate.get(delegate);
        if (objectsDelegateResponsibleFor != null) {
            objectsDelegateResponsibleFor.remove(objectId);
                synchronized (objectsByDelegate) { // todo
                    if (objectsDelegateResponsibleFor.isEmpty()) {
                       objectsByDelegate.remove(delegate);
                    }
                }
        }
    }

    public Id getDelegate(Id id) {
        return delegateById.get(id);
    }

    public Set<Id> getObjectsByDelegate(Id accessCheckDelegateId) {
        final SizeableConcurrentHashMap<Id, Id> delegatesMap = objectsByDelegate.get(accessCheckDelegateId);
        return delegatesMap == null ? null : delegatesMap.keySet();
    }

    private SizeableConcurrentHashMap<Id, Id> findOrCreateObjectsByDelegate(Id accessCheckDelegateId) {
        SizeableConcurrentHashMap<Id, Id> objectsForDelegate = objectsByDelegate.get(accessCheckDelegateId);
        if (objectsForDelegate == null) {
            objectsForDelegate = new SizeableConcurrentHashMap<>(16, 0.75f, 16, null, false, false);
            SizeableConcurrentHashMap<Id, Id> previous = objectsByDelegate.putIfAbsent(accessCheckDelegateId, objectsForDelegate);
            if (previous != null) {
                objectsForDelegate = previous;
            }
        }
        return objectsForDelegate;
    }

    @Override
    public Size getSize() {
        return new Size(delegateById.getSize().get() + objectsByDelegate.getSize().get());
    }
}
