package ru.intertrust.cm.globalcache.impl.localjvm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class SynchronizedGlobalCacheImpl extends LockManagerGlobalCacheImpl{
    private static final Logger logger = LoggerFactory.getLogger(SynchronizedGlobalCacheImpl.class);

    @Autowired
    private ReentrantReadWriteLockManager lockManager;

    @Override
    protected LockManager getLockManager() {
        return lockManager;
    }
}
