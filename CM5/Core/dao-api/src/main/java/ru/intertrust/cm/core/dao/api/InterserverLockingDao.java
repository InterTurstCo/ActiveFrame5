package ru.intertrust.cm.core.dao.api;

import java.util.Date;

public interface InterserverLockingDao {

    boolean lock(String resourceId, Date lockTime);

    void unlock(String resourceId);

    Date getLastLockTime(String resourceId);

    void updateLock(String resourceId, Date lockTime);

    /**
     * Обновление отсечки времени блокировки по составному ключу (идентификатор
     * + старая отсечка времени).
     * @param resourceId
     *            идентификатор ресурса
     * @param oldLockTime
     *            старая отсечка времени
     * @param lockTime
     *            новая отсечка времени
     */
    void updateLock(String resourceId, Date oldLockTime, Date lockTime);

    boolean unlock(String resourceId, Date lockTime);

}
