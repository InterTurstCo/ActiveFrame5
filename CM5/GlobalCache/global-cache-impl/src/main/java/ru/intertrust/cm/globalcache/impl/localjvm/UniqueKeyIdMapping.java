package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.globalcache.api.AbsentDomainObject;
import ru.intertrust.cm.globalcache.api.UniqueKey;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Denis Mitavskiy
 *         Date: 17.08.2015
 *         Time: 15:09
 */
public class UniqueKeyIdMapping {
    private final Object ABSENT_KEYS_LOCK = new Object();

    private ConcurrentMap<UniqueKey, Id> idByUniqueKey;
    private ConcurrentMap<UniqueKey, DomainObject> absentUniqueKeys;
    private ConcurrentMap<Id, Set<UniqueKey>> uniqueKeyById;

    public UniqueKeyIdMapping(int initialCapacity, int concurrentcyLevel) {
        idByUniqueKey = new ConcurrentHashMap<>(initialCapacity, 0.75f, concurrentcyLevel);
        uniqueKeyById = new ConcurrentHashMap<>(initialCapacity, 0.75f, concurrentcyLevel);
        absentUniqueKeys = new ConcurrentHashMap<>(100, 0.75f, concurrentcyLevel);
    }

    public void updateMappings(DomainObject obj, Collection<UniqueKey> allUniqueKeys) {
        final Id id = obj.getId();
        for (UniqueKey key : allUniqueKeys) {
            absentUniqueKeys.remove(key);
        }
        Set<UniqueKey> existingUniqueKeys = this.uniqueKeyById.get(id);
        if (existingUniqueKeys == null) {
            return;
        } else {
            for (UniqueKey existingUniqueKey : existingUniqueKeys) {
                if (!obj.containsFieldValues(existingUniqueKey.getValues())) {
                    this.idByUniqueKey.remove(existingUniqueKey);
                }
            }
        }
    }

    public void setMapping(DomainObject obj, UniqueKey uniqueKey) {
        if (obj == null) {
            absentUniqueKeys.put(uniqueKey, AbsentDomainObject.INSTANCE);
            return;
        }

        final Id id = obj.getId();
        Set<UniqueKey> existingUniqueKeys = this.uniqueKeyById.get(id);
        if (existingUniqueKeys == null) {
            existingUniqueKeys = Collections.synchronizedSet(new HashSet<UniqueKey>());
            this.uniqueKeyById.put(id, existingUniqueKeys);
        }
        existingUniqueKeys.add(uniqueKey);
        idByUniqueKey.put(uniqueKey, id);
    }

    public void clear(Id id) {
        Set<UniqueKey> existingUniqueKeys = this.uniqueKeyById.get(id);
        if (existingUniqueKeys != null) {
            for (UniqueKey existingUniqueKey : existingUniqueKeys) {
                this.idByUniqueKey.remove(existingUniqueKey);
            }
            this.uniqueKeyById.remove(id);
        }
    }

    public boolean isNullValue(UniqueKey uniqueKey) {
        return absentUniqueKeys.containsKey(uniqueKey);
    }

    public void clearNullValue(UniqueKey uniqueKey) {
        absentUniqueKeys.remove(uniqueKey);
    }

    public Id getId(UniqueKey uniqueKey) {
        return idByUniqueKey.get(uniqueKey);
    }
}
