package ru.intertrust.cm.globalcache.impl.localjvm;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Denis Mitavskiy
 *         Date: 17.08.2015
 *         Time: 19:05
 */
public class TypeUniqueKeyMapping {
    private ConcurrentMap<String, UniqueKeyIdMapping> uniqueKeyMappingByType;
    private int concurrencyLevel;

    public TypeUniqueKeyMapping(int concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
        this.uniqueKeyMappingByType = new ConcurrentHashMap<>(100, 0.75f, concurrencyLevel);
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
}
