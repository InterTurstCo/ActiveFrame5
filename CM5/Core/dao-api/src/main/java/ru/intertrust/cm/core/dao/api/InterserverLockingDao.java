package ru.intertrust.cm.core.dao.api;

import java.util.Date;

public interface InterserverLockingDao {

    boolean lock(String resourceId, Date lockTime);

    void unlock(String resourceId);

    Date getLastLockTime(String resourceId);

    void updateLock(String resourceId, Date lockTime);

    boolean unlock(String resourceId, Date lockTime);

}
