package ru.intertrust.cm.core.business.api;

public interface InterserverLockingService {

    void lock(String resourceId);

    boolean isLocked(String resourceId);

    void unlock(String resourceId);

    boolean tryLock(String resourceId);

}
