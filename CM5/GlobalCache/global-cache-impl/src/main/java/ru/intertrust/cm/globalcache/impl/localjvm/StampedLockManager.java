package ru.intertrust.cm.globalcache.impl.localjvm;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.StampedLock;

public class StampedLockManager extends LockManagerBase {

    private GlobalCacheLockApi globalReadLock = new SingleReadLockImpl(new StampedLock());
    private GlobalCacheLockApi globalWriteLock = new SingleWriteLockImpl((SingleReadLockImpl)globalReadLock);
    private GlobalCacheLockApi globalAccessReadLock = new SingleReadLockImpl(new StampedLock());
    private GlobalCacheLockApi globalAccessWriteLock = new SingleWriteLockImpl((SingleReadLockImpl)globalAccessReadLock); // TODO ранее передавалось globalReadLock, непонятно почему

    @Override
    protected GlobalCacheLockApi getGlobalReadLock(){
        return globalReadLock;
    }

    @Override
    protected GlobalCacheLockApi getGlobalWriteLock(){
        return globalWriteLock;
    }

    @Override
    protected GlobalCacheLockApi getGlobalAccessReadLock(){
        return globalAccessReadLock;
    }

    @Override
    protected GlobalCacheLockApi getGlobalAccessWriteLock(){
        return globalAccessWriteLock;
    }

    @Override
    protected GlobalCacheReadWriteLock createGlobalCacheReadWriteLock(){
        return new GlobalCacheReadWriteLock(){
            StampedLock lock = new StampedLock();
            @Override
            public GlobalCacheLockApi getReadLock() {
                return new SingleReadLockImpl(lock);
            }

            @Override
            public GlobalCacheLockApi getWriteLock() {
                return new SingleWriteLockImpl(lock);
            }
        };
    }

    private class SingleReadLockImpl implements GlobalCacheLockApi {
        private StampedLock stampedLock;
        private long lock = 0;

        private SingleReadLockImpl(StampedLock stampedLock) {
            this.stampedLock = stampedLock;
        }

        @Override
        public GlobalCacheLockApi lock() {
            lock = stampedLock.readLock();
            return this;
        }

        @Override
        public void unlock() {
            stampedLock.unlock(lock);
        }
    }

    private class SingleWriteLockImpl implements GlobalCacheLockApi {
        private StampedLock stampedLock;
        private long lock = 0;

        private SingleWriteLockImpl(StampedLock stampedLock) {
            this.stampedLock = stampedLock;
        }

        private SingleWriteLockImpl(SingleReadLockImpl from) {
            this.stampedLock = from.stampedLock;
        }

        @Override
        public GlobalCacheLockApi lock() {
            lock = stampedLock.writeLock();
            return this;
        }

        @Override
        public void unlock() {
            stampedLock.unlock(lock);
        }
    }

}
