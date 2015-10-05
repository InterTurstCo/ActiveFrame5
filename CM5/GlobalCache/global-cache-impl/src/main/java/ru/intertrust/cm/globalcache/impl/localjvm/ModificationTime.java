package ru.intertrust.cm.globalcache.impl.localjvm;

/**
 * @author Denis Mitavskiy
 *         Date: 28.07.2015
 *         Time: 17:40
 */
public class ModificationTime {
    private long saveTime;
    private long objectModifiedTime;

    public ModificationTime(long saveTime, long objectModifiedTime) {
        this.saveTime = saveTime;
        this.objectModifiedTime = objectModifiedTime;
    }

    public synchronized void update(long saveTime, long objectModifiedTime) {
        if (saveTime > this.saveTime) {
            this.saveTime = saveTime;
        }
        if (objectModifiedTime > this.objectModifiedTime) {
            this.objectModifiedTime = objectModifiedTime;
        }
    }

    public synchronized boolean before(long time) {
        return saveTime < time && objectModifiedTime < time;
    }

    public synchronized boolean afterOrSame(long time) {
        return saveTime >= time && objectModifiedTime >= time;
    }
}
