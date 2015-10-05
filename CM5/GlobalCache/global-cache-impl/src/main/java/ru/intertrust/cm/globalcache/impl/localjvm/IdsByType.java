package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Denis Mitavskiy
 *         Date: 20.08.2015
 *         Time: 19:56
 */
public class IdsByType {
    private ConcurrentMap<String, ConcurrentMap<Id, Id>> idByType;

    public IdsByType(int concurrencyLevel, int size) {
        idByType = new ConcurrentHashMap<>(size, 0.75f, concurrencyLevel);
    }

    public void setIdType(Id id, String type) {
        String lowercasedType = type.toLowerCase();
        ConcurrentMap<Id, Id> mapping = idByType.get(lowercasedType);
        if (mapping == null) {
            mapping = new ConcurrentHashMap<>(100);
            final ConcurrentMap<Id, Id> newMap = idByType.putIfAbsent(lowercasedType, mapping);
            mapping = newMap == null ? mapping : newMap;
        }
        mapping.put(id, id);
    }

    public ConcurrentMap<Id, Id> getIds(String type) {
        return idByType.get(type.toLowerCase());
    }
}
