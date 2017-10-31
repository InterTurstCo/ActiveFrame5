package ru.intertrust.cm.globalcache.impl.localjvm;

/**
 * @author Denis Mitavskiy
 *         Date: 28.07.2015
 *         Time: 17:40
 */
public class ModificationTime {
    private volatile long saveTime;
    private volatile long objectModifiedTime;
    private volatile long rightsChangedTime;

    public ModificationTime(long saveTime, long objectModifiedTime, long rightsChangedTime) {
        this.saveTime = saveTime;
        this.objectModifiedTime = objectModifiedTime;
        this.rightsChangedTime = rightsChangedTime;
    }

    public synchronized void updateSaveTime(long saveTime, long objectModifiedTime) {
        if (saveTime > this.saveTime) {
            this.saveTime = saveTime;
        } else {
            ++this.saveTime;
        }
        if (objectModifiedTime > this.objectModifiedTime) {
            this.objectModifiedTime = objectModifiedTime;
        } else {
            ++this.objectModifiedTime;
        }
    }

    public synchronized void updateRightsChangeTime(long time) {
        if (time > rightsChangedTime) {
            this.rightsChangedTime = time;
        } else {
            ++this.rightsChangedTime;
        }
    }

    public synchronized boolean before(long time) {
        return saveTime < time && objectModifiedTime < time;
    }

    public synchronized boolean lastSaveAfterOrEqual(long time) {
        return saveTime >= time || objectModifiedTime >= time;
    }

    public synchronized boolean lastChangeAfterOrEqual(long time) {
        return lastSaveAfterOrEqual(time) || rightsChangedTime >= time;
    }

    public long getSaveTime() {
        return Math.max(saveTime, objectModifiedTime);
    }

    public long getModificationTime() {
        return Math.max(saveTime, Math.max(objectModifiedTime, rightsChangedTime));
    }
}
