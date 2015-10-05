package ru.intertrust.cm.globalcache.impl.localjvm;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 28.07.2015
 *         Time: 18:02
 */
public class DomainObjectTypeSaveTime {
    private ConcurrentHashMap<String, ModificationTime> doTypeLastSaveTime;

    public DomainObjectTypeSaveTime(int objectsQty) {
        this.doTypeLastSaveTime = new ConcurrentHashMap<>((int) (objectsQty / 0.75 + 1));
    }

    public void setLastModificationTime(String type, long saveTime, long objectModifiedDate) {
        type = type.toLowerCase();
        ModificationTime previousTime = doTypeLastSaveTime.get(type);
        if (previousTime == null) {
            final ModificationTime putResult = doTypeLastSaveTime.putIfAbsent(type, new ModificationTime(saveTime, objectModifiedDate));
            if (putResult == null) {
                return;
            }
            previousTime = putResult;
        }
        previousTime.update(saveTime, objectModifiedDate);
    }

    public ModificationTime getLastModificationTime(String type) {
        return doTypeLastSaveTime.get(type.toLowerCase());
    }
}
