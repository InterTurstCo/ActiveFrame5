package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.Case;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 28.07.2015
 *         Time: 18:02
 */
public class DomainObjectTypeChangeTime {
    private ConcurrentHashMap<String, ModificationTime> doTypeLastSaveTime;

    public DomainObjectTypeChangeTime(int objectsQty) {
        this.doTypeLastSaveTime = new ConcurrentHashMap<>((int) (objectsQty / 0.75 + 1));
    }

    public void setLastModificationTime(String type, long saveTime, long objectModifiedDate) {
        type = Case.toLower(type);
        ModificationTime previousTime = doTypeLastSaveTime.get(type);
        if (previousTime == null) {
            final ModificationTime putResult = doTypeLastSaveTime.putIfAbsent(type, new ModificationTime(saveTime, objectModifiedDate, -1));
            if (putResult == null) {
                return;
            }
            previousTime = putResult;
        }
        previousTime.updateSaveTime(saveTime, objectModifiedDate);
    }

    public void setLastRightsChangeTime(String type, long rightsChangeTime) {
        type = Case.toLower(type);
        ModificationTime previousTime = doTypeLastSaveTime.get(type);
        if (previousTime == null) {
            final ModificationTime putResult = doTypeLastSaveTime.putIfAbsent(type, new ModificationTime(-1, -1, rightsChangeTime));
            if (putResult == null) {
                return;
            }
            previousTime = putResult;
        }
        previousTime.updateRightsChangeTime(rightsChangeTime);
    }

    public ModificationTime getLastModificationTime(String type) {
        return doTypeLastSaveTime.get(Case.toLower(type));
    }
}
