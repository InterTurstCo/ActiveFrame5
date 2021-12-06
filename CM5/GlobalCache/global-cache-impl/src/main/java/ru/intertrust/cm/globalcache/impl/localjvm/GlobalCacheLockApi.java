package ru.intertrust.cm.globalcache.impl.localjvm;

interface GlobalCacheLockApi extends GlobalCacheLock {
    GlobalCacheLockApi lock();
    void unlock();
}
