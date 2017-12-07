package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
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
    private Size size;

    public TypeUniqueKeyMapping(int concurrencyLevel, Size cacheTotal) {
        size = new Size(cacheTotal);
        size.add(4 * SizeEstimator.REFERENCE_SIZE);

        this.concurrencyLevel = concurrencyLevel;
        this.uniqueKeyMappingByType = new SizeableConcurrentHashMap<>(100, 0.75f, concurrencyLevel, size, true, true);
    }

    public UniqueKeyIdMapping getUniqueKeyIdMapping(String type) {
        return type == null ? null : uniqueKeyMappingByType.get(Case.toLower(type));
    }

    public UniqueKeyIdMapping getOrCreateUniqueKeyIdMapping(String type) {
        final String lowercasedType = Case.toLower(type);
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
        return size;
    }
}
