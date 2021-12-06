package ru.intertrust.cm.globalcache.impl.localjvm;

public interface GlobalCacheReadWriteLock {
    GlobalCacheLockApi getReadLock();
    GlobalCacheLockApi getWriteLock();
}
