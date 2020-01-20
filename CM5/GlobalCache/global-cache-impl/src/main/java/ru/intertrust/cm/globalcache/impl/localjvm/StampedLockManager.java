package ru.intertrust.cm.globalcache.impl.localjvm;

public class StampedLockManager extends LockManagerBase {

    @Override
    protected GlobalCacheLockApi getGlobalReadLock() {
        return null;
    }

    @Override
    protected GlobalCacheLockApi getGlobalWriteLock() {
        return null;
    }

    @Override
    protected GlobalCacheLockApi getGlobalAccessReadLock() {
        return null;
    }

    @Override
    protected GlobalCacheLockApi getGlobalAccessWriteLock() {
        return null;
    }

    @Override
    protected GlobalCacheReadWriteLock createGlobalCacheReadWriteLock() {
        return null;
    }
}
