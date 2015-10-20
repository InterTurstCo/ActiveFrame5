package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.dao.dto.CollectionTypesKey;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.Sizeable;
import ru.intertrust.cm.globalcache.api.util.SizeableConcurrentHashMap;

import java.util.Map;
import java.util.Set;

/**
 * @author Denis Mitavskiy
 *         Date: 13.08.2015
 *         Time: 18:27
 */
public class CollectionsTree implements Sizeable {
    private SizeableConcurrentHashMap<CollectionTypesKey, CollectionBaseNode> collections;

    public CollectionsTree(int initialCapacity, int concurrencyLevel, Size totalCacheSize) {
        this(initialCapacity, 0.75f, concurrencyLevel, totalCacheSize);
    }

    public CollectionsTree(int initialCapacity, float loadFactor, int concurrencyLevel, Size totalCacheSize) {
        collections = new SizeableConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel, totalCacheSize, true, true);
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

    public Set<Map.Entry<CollectionTypesKey, CollectionBaseNode>> getEntries() {
        return collections.entrySet();
    }

    public void removeBaseNode(CollectionTypesKey key) {
        collections.remove(key);
    }

    @Override
    public Size getSize() {
        return collections.getSize();
    }
}
