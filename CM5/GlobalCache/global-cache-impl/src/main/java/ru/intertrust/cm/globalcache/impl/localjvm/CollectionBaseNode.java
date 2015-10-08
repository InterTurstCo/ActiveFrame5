package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.globalcache.api.CollectionSubKey;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;
import ru.intertrust.cm.globalcache.api.util.SizeableConcurrentHashMap;

import java.util.Set;

/**
 * @author Denis Mitavskiy
 *         Date: 11.08.2015
 *         Time: 16:13
 */
public class CollectionBaseNode implements Sizeable {
    private Set<String> collectionTypes;
    private SizeableConcurrentHashMap<CollectionSubKey, CollectionNode> collections; // collection sub-key

    private Size size;

    public CollectionBaseNode(Set<String> collectionTypes) {
        this.collectionTypes = collectionTypes;
        size = new Size(2 * SizeEstimator.getReferenceSize() + SizeEstimator.estimateSize(collectionTypes));

        collections = new SizeableConcurrentHashMap<>(16, 0.75f, 16, size, true, true);
    }

    public CollectionNode setCollectionNode(CollectionSubKey key, CollectionNode node) {
        final CollectionNode existingNode = collections.putIfAbsent(key, node);
        if (existingNode == null) {
            return node;
        } else {
            if (node.getTimeRetrieved() > existingNode.getTimeRetrieved()) {
                collections.remove(key);
                return setCollectionNode(key, node);
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

    @Override
    public Size getSize() {
        return size;
    }
}
