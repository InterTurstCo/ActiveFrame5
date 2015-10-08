package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.Sizeable;
import ru.intertrust.cm.globalcache.api.util.SizeableConcurrentHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 17.08.2015
 *         Time: 19:05
 */
public class TypeUniqueKeyMapping implements Sizeable {
    private SizeableConcurrentHashMap<String, UniqueKeyIdMapping> uniqueKeyMappingByType;
    private int concurrencyLevel;
    private Size cacheTotal;

    public TypeUniqueKeyMapping(int concurrencyLevel, Size cacheTotal) {
        this.concurrencyLevel = concurrencyLevel;
        this.cacheTotal = cacheTotal;
        this.uniqueKeyMappingByType = new SizeableConcurrentHashMap<>(100, 0.75f, concurrencyLevel, cacheTotal, true, true);
    }

    public UniqueKeyIdMapping getUniqueKeyIdMapping(String type) {
        return uniqueKeyMappingByType.get(type.toLowerCase());
    }

    public UniqueKeyIdMapping getOrCreateUniqueKeyIdMapping(String type) {
        final String lowercasedType = type.toLowerCase();
        final UniqueKeyIdMapping mapping = uniqueKeyMappingByType.get(lowercasedType);
        if (mapping != null) {
            return mapping;
        }
        final UniqueKeyIdMapping uniqueKeyIdMapping = new UniqueKeyIdMapping(1000, concurrencyLevel);
        final UniqueKeyIdMapping result = uniqueKeyMappingByType.putIfAbsent(lowercasedType, uniqueKeyIdMapping);
        return result == null ? uniqueKeyIdMapping : result;
    }

    @Override
    public Size getSize() {
        return null;
    }
}
