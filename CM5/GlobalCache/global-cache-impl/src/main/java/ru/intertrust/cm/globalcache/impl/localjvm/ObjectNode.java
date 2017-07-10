package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;
import ru.intertrust.cm.globalcache.api.util.SizeableConcurrentHashMap;

import java.util.concurrent.ConcurrentMap;

import static ru.intertrust.cm.globalcache.api.util.SizeEstimator.estimateSize;

/**
 * @author Denis Mitavskiy
 *         Date: 09.07.2015
 *         Time: 18:19
 */
public class ObjectNode implements Sizeable {
    public static final long SELF_SIZE = 5 * SizeEstimator.REFERENCE_SIZE + Long.SIZE;
    public static final long USER_SUBJECT_SIZE = SizeEstimator.estimateSize(new UserSubject(1));

    public enum LinkedObjects {
        System,
        User,
        All
    }
    // Id is here in order to support such scenarios:
    // 1) Domain Object doesn't exist
    // 2) Domain Object is never accessed by System, but by User only, and this User doesn't have access
    private volatile Id id; // null-id means it's been removed by Cache Cleaner

    // If domainObject == null means that we don't actually know if object exists - it doesn't have access for everyone, it has never been accessed by System
    // or by someone who has right to read it.
    // If domainObject == DomainObject.NULL it means it doesn't exist
    private volatile DomainObject domainObject;
    private volatile long domainObjectSize;
    private volatile SizeableConcurrentHashMap<LinkedObjectsKey, LinkedObjectsNode> systemLinkedObjects;
    private volatile SizeableConcurrentHashMap<LinkedObjectsKey, SizeableConcurrentHashMap<UserSubject, LinkedObjectsNode>> userLinkedObjects;

    private Size size;

    // domainObject == null - means that we don't know if this object exists (it has been accesses by User, but why it's null - no rights, or not exists we don't know)
    // domainObject == DomainObject.NULL - object doesn't exist

    public ObjectNode(Id id, DomainObject domainObject) {
        this.id = id;
        this.domainObject = domainObject;
        this.domainObjectSize = estimateSize(domainObject);
        this.size = new Size(SELF_SIZE + estimateSize(id) + domainObjectSize);
    }

    @Override
    public Size getSize() {
        return size;
    }

    public Id getId() {
        return id;
    }

    public void setDomainObject(DomainObject domainObject) {
        long prevSize = this.domainObjectSize;
        this.domainObjectSize = estimateSize(domainObject);
        this.domainObject = domainObject;
        this.size.add(this.domainObjectSize - prevSize);
    }

    public DomainObject getDomainObject() {
        return domainObject;
    }

    public DomainObject getRealDomainObjectOrNothing() {
        return domainObject == null ? null : domainObject.isAbsent() ? null : domainObject;
    }

    public void setSystemLinkedObjectsNode(LinkedObjectsKey key, LinkedObjectsNode node) {
        if (systemLinkedObjects == null) {
            synchronized (this) {
                if (systemLinkedObjects == null) {
                    systemLinkedObjects = new SizeableConcurrentHashMap<>(size);
                }
            }
        }
        systemLinkedObjects.put(key, node);
    }

    public void setUserLinkedObjectsNode(LinkedObjectsKey key, LinkedObjectsNode node, UserSubject user) {
        if (userLinkedObjects == null) {
            synchronized (this) {
                if (userLinkedObjects == null) {
                    userLinkedObjects = new SizeableConcurrentHashMap<>(size);
                }
            }
        }
        SizeableConcurrentHashMap<UserSubject, LinkedObjectsNode> linkedObjectsPerUser = userLinkedObjects.get(key);
        if (linkedObjectsPerUser == null) {
            linkedObjectsPerUser = new SizeableConcurrentHashMap<>();
            final SizeableConcurrentHashMap<UserSubject, LinkedObjectsNode> previous = userLinkedObjects.putIfAbsent(key, linkedObjectsPerUser);
            if (previous != null) {
                linkedObjectsPerUser = previous;
            }
        }
        linkedObjectsPerUser.put(user, node);
    }

    public void clearLinkedObjects(LinkedObjectsKey key, LinkedObjects objects) {
        switch (objects) {
            case User:
                clearUserLinkedObjects(key);
                return;
            case System:
                clearSystemLinkedObjects(key);
                return;
            default:
                clearUserLinkedObjects(key);
                clearSystemLinkedObjects(key);
        }
    }

    public void clearSystemLinkedObjects(LinkedObjectsKey key) {
        if (systemLinkedObjects != null) {
            systemLinkedObjects.remove(key);
        }
    }

    public void clearUserLinkedObjects(LinkedObjectsKey key) {
        if (userLinkedObjects != null) {
            userLinkedObjects.remove(key);
        }
    }

    public void clearUserLinkedObjects(UserSubject user) {
        for (SizeableConcurrentHashMap<UserSubject, LinkedObjectsNode> userNodes : userLinkedObjects.values()) {
            userNodes.remove(user);
        }
    }

    public void clearAllUsersLinkedObjects() {
        if (userLinkedObjects == null) {
            return;
        }
        synchronized (this) {
            if (userLinkedObjects == null) {
                userLinkedObjects = new SizeableConcurrentHashMap<>(size);
            }
        }
        userLinkedObjects.getSize().detachFromTotal();
    }

    public LinkedObjectsNode getLinkedObjectsNode(LinkedObjectsKey key, UserSubject user) {
        if (user == null) {
            return systemLinkedObjects == null ? null : systemLinkedObjects.get(key);
        } else {
            if (userLinkedObjects == null) {
                return null;
            }
            final ConcurrentMap<UserSubject, LinkedObjectsNode> linkedObjectsByUser = userLinkedObjects.get(key);
            if (linkedObjectsByUser == null) {
                return null;
            }
            return linkedObjectsByUser.get(user);
        }
    }



    public boolean toBeSweeped() {
        return domainObject == null;
    }

    public void markForSweep() { // todo?

    }
}
