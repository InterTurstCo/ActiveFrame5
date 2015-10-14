package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeableConcurrentHashMap;

import java.util.concurrent.ConcurrentMap;

/**
 * @author Denis Mitavskiy
 *         Date: 20.08.2015
 *         Time: 19:56
 */
public class IdsByType {
    private SizeableConcurrentHashMap<String, SizeableConcurrentHashMap<Id, Id>> idByType;

    public IdsByType(int concurrencyLevel, int size, Size totalCacheSize) {
        idByType = new SizeableConcurrentHashMap<>(size, 0.75f, concurrencyLevel, totalCacheSize, false, false);
    }

    public void setIdType(Id id, String type) {
        String lowercasedType = type.toLowerCase();
        SizeableConcurrentHashMap<Id, Id> mapping = idByType.get(lowercasedType);
        if (mapping == null) {
            mapping = new SizeableConcurrentHashMap<>(100, 0.75f, 16, null, false, false);
            final SizeableConcurrentHashMap<Id, Id> newMap = idByType.putIfAbsent(lowercasedType, mapping);
            mapping = newMap == null ? mapping : newMap;
        }
        mapping.put(id, id);
    }

    public void removeId(Id id, String type) {
        String lowercasedType = type.toLowerCase();
        SizeableConcurrentHashMap<Id, Id> mapping = idByType.get(lowercasedType);
        if (mapping == null) {
            return;
        }
        mapping.remove(id);
    }

    public ConcurrentMap<Id, Id> getIds(String type) {
        return idByType.get(type.toLowerCase());
    }
}
