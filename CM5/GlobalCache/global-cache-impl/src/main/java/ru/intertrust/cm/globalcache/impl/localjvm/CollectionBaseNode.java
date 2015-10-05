package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.globalcache.api.CollectionSubKey;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Denis Mitavskiy
 *         Date: 11.08.2015
 *         Time: 16:13
 */
public class CollectionBaseNode {
    private Set<String> collectionTypes;
    private ConcurrentMap<CollectionSubKey, CollectionNode> collections; // collection sub-key

    public CollectionBaseNode(Set<String> collectionTypes) {
        this.collectionTypes = collectionTypes;
        collections = new ConcurrentHashMap<>(16, 0.75f, 16);
    }

    public CollectionNode setCollectionNode(CollectionSubKey key, CollectionNode node) {
        final CollectionNode existingNode = collections.putIfAbsent(key, node);
        if (existingNode == null) {
            return node;
        } else {
            if (node.getTimeRetrieved() > existingNode.getTimeRetrieved()) {
                collections.put(key, node);
            }
            return existingNode;
        }
    }

    public CollectionNode getCollectionNode(CollectionSubKey key) {
        return collections.get(key);
    }

    public Set<String> getCollectionTypes() {
        return collectionTypes;
    }
}
