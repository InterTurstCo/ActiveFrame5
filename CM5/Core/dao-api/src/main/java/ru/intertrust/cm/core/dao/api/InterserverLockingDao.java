package ru.intertrust.cm.core.dao.api;

import java.util.Date;

public interface InterserverLockingDao {

    void lock(String resourceId, Date lockTime);

    boolean wasLockedAfter(String resourceId, Date time);

    void unlock(String resourceId);

    Date getLastLockTime(String resourceId);

}
