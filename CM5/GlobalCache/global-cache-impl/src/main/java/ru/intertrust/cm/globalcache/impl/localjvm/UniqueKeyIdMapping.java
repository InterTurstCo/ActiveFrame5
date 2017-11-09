package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.globalcache.api.UniqueKey;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;
import ru.intertrust.cm.globalcache.api.util.SizeableConcurrentHashMap;

import java.util.Collection;

/**
 * @author Denis Mitavskiy
 *         Date: 17.08.2015
 *         Time: 15:09
 */
public class UniqueKeyIdMapping implements Sizeable {
    private Size size; // total cache size will be set to this object when it's added to the parent map

    private SizeableConcurrentHashMap<UniqueKey, Id> idByUniqueKey;
    private SizeableConcurrentHashMap<UniqueKey, UniqueKey> absentUniqueKeys;
    private SizeableConcurrentHashMap<Id, SizeableConcurrentHashMap<UniqueKey, UniqueKey>> uniqueKeyById;

    public UniqueKeyIdMapping(int initialCapacity, int concurrentcyLevel) {
        size = new Size(4 * SizeEstimator.REFERENCE_SIZE);
        // all sizes will be calculated manually
        idByUniqueKey = new SizeableConcurrentHashMap<>(initialCapacity, 0.75f, concurrentcyLevel, size, true, true);
        absentUniqueKeys = new SizeableConcurrentHashMap<>(100, 0.75f, concurrentcyLevel, size, true, true);
        uniqueKeyById = new SizeableConcurrentHashMap<>(initialCapacity, 0.75f, concurrentcyLevel, size, false, false);
    }

    public void updateMappings(DomainObject obj, Collection<UniqueKey> allUniqueKeys) {
        final Id id = obj.getId();
        for (UniqueKey key : allUniqueKeys) {
            absentUniqueKeys.remove(key);
        }
        SizeableConcurrentHashMap<UniqueKey, UniqueKey> existingUniqueKeys = this.uniqueKeyById.get(id);
        if (existingUniqueKeys == null) {
            return;
        } else {
            for (UniqueKey existingUniqueKey : existingUniqueKeys.keySet()) {
                if (!obj.containsFieldValues(existingUniqueKey.getValues())) {
                    this.idByUniqueKey.remove(existingUniqueKey);
                }
            }
        }
    }

    public void setMapping(DomainObject obj, UniqueKey uniqueKey) {
        if (obj == null) {
            absentUniqueKeys.put(uniqueKey, uniqueKey);
            return;
        }

        final Id id = obj.getId();
        SizeableConcurrentHashMap<UniqueKey, UniqueKey> existingUniqueKeys = this.uniqueKeyById.get(id);
        if (existingUniqueKeys == null) {
            existingUniqueKeys = new SizeableConcurrentHashMap<>(16, 0.75f, 16, null, false, false);
            this.uniqueKeyById.put(id, existingUniqueKeys);
        }
        existingUniqueKeys.put(uniqueKey, uniqueKey);
        idByUniqueKey.put(uniqueKey, id);
    }

    public void clear(Id id) {
        SizeableConcurrentHashMap<UniqueKey, UniqueKey> existingUniqueKeys = this.uniqueKeyById.get(id);
        if (existingUniqueKeys != null) {
            for (UniqueKey existingUniqueKey : existingUniqueKeys.keySet()) {
                this.idByUniqueKey.remove(existingUniqueKey);
            }
            this.uniqueKeyById.remove(id);
        }
    }

    public boolean isNullValue(UniqueKey uniqueKey) {
        return absentUniqueKeys.containsKey(uniqueKey);
    }

    public void clear(UniqueKey uniqueKey) {
        final Id removedId = idByUniqueKey.remove(uniqueKey);
        if (removedId != null) {
            final SizeableConcurrentHashMap<UniqueKey, UniqueKey> existingUniqueKeys = uniqueKeyById.get(removedId);
            if (existingUniqueKeys != null) {
                existingUniqueKeys.remove(uniqueKey);
            }
        }
        clearNullValue(uniqueKey);
    }

    public void clearNullValue(UniqueKey uniqueKey) {
        absentUniqueKeys.remove(uniqueKey);
    }

    public Id getId(UniqueKey uniqueKey) {
        return idByUniqueKey.get(uniqueKey);
    }

    @Override
    public Size getSize() {
        return size;
    }
}
