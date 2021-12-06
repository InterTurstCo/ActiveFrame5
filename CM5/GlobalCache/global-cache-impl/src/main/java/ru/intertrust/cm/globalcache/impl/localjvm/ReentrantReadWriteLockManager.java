package ru.intertrust.cm.globalcache.impl.localjvm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReentrantReadWriteLockManager extends LockManagerBase {
    private static final Logger logger = LoggerFactory.getLogger(ReentrantReadWriteLockManager.class);

    private GlobalCacheLockApi globalReadLock = new SingleReadLockImpl(new ReentrantReadWriteLock(true));
    private GlobalCacheLockApi globalWriteLock = new SingleWriteLockImpl((SingleReadLockImpl)globalReadLock);
    private GlobalCacheLockApi globalAccessReadLock = new SingleReadLockImpl(new ReentrantReadWriteLock(true));
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
            ReentrantReadWriteLock lock = new ReentrantReadWriteLock(true);
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
        private final ReentrantReadWriteLock.ReadLock lock;

        private SingleReadLockImpl(ReentrantReadWriteLock lock) {
            this.lock = lock.readLock();
        }

        @Override
        public GlobalCacheLockApi lock() {
            lock.lock();
            return this;
        }

        @Override
        public void unlock() {
            lock.unlock();
        }
    }

    private class SingleWriteLockImpl implements GlobalCacheLockApi {
        private final ReentrantReadWriteLock.ReadLock lock;

        private SingleWriteLockImpl(ReentrantReadWriteLock lock) {
            this.lock = lock.readLock();
        }

        private SingleWriteLockImpl(SingleReadLockImpl from) {
            this.lock = from.lock;
        }

        @Override
        public GlobalCacheLockApi lock() {
            lock.lock();
            return this;
        }

        @Override
        public void unlock() {
            lock.unlock();
        }
    }


}
