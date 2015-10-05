package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.dao.dto.CollectionTypesKey;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Denis Mitavskiy
 *         Date: 13.08.2015
 *         Time: 18:27
 */
public class CollectionsTree {
    private ConcurrentMap<CollectionTypesKey, CollectionBaseNode> collections;

    public CollectionsTree(int initialCapacity, int concurrencyLevel) {
        this(initialCapacity, 0.75f, concurrencyLevel);
    }

    public CollectionsTree(int initialCapacity, float loadFactor, int concurrencyLevel) {
        collections = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }

    public Set<String> getDomainObjectTypesInvolved(CollectionTypesKey key) {
        final CollectionBaseNode node = collections.get(key);
        if (node == null) {
            return null;
        }
        return node.getCollectionTypes();
    }

    public CollectionBaseNode addBaseNode(CollectionTypesKey key, CollectionBaseNode node) {
        final CollectionBaseNode result = collections.putIfAbsent(key, node);
        return result == null ? node : result;
    }

    public CollectionBaseNode getBaseNode(CollectionTypesKey key) {
        return collections.get(key);
    }

    public void removeBaseNode(CollectionTypesKey key) {
        collections.remove(key);
    }
}
