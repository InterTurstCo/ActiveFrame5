package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;
import ru.intertrust.cm.globalcache.api.util.SizeableConcurrentHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 09.07.2015
 *         Time: 18:02
 */
public class ObjectsTree implements Sizeable {
    private static final long SELF_SIZE = 2 * SizeEstimator.REFERENCE_SIZE;

    private Size cacheTotalSize;
    private SizeableConcurrentHashMap<Id, ObjectNode> domainObjects;

    public ObjectsTree(int initialCapacity, int concurrencyLevel, Size cacheTotalSize) {
        this(initialCapacity, 0.75f, concurrencyLevel, cacheTotalSize);
    }

    public ObjectsTree(int initialCapacity, float loadFactor, int concurrencyLevel, Size cacheTotalSize) {
        this.cacheTotalSize = cacheTotalSize;
        this.cacheTotalSize.add(SELF_SIZE);
        domainObjects = new SizeableConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel, cacheTotalSize, true, true);
    }

    public ObjectNode addDomainObjectNode(Id id, ObjectNode node) {
        final ObjectNode existingNode = domainObjects.putIfAbsent(id, node);
        return existingNode == null ? node : existingNode;
    }

    public ObjectNode getDomainObjectNode(Id id) {
        return domainObjects.get(id);
    }

    public ObjectNode deleteDomainObjectNode(Id id) {
        return domainObjects.remove(id);
    }

    public void clearUserLinkedObjects(UserSubject user) {
        for (ObjectNode objectNode : domainObjects.values()) {
            objectNode.clearUserLinkedObjects(user);
        }
    }

    public void clearAllUsersLinkedObjects() {
        for (ObjectNode objectNode : domainObjects.values()) {
            objectNode.clearAllUsersLinkedObjects();
        }
    }

    @Override
    public Size getSize() {
        return domainObjects.getSize();
    }
}
