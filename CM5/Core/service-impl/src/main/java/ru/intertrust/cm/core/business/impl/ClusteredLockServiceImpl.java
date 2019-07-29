package ru.intertrust.cm.core.business.impl;

import java.time.Duration;
import java.util.Set;

import ru.intertrust.cm.core.business.api.ClusteredLockService;
import ru.intertrust.cm.core.dao.api.clusterlock.ClusteredLock;

public class ClusteredLockServiceImpl implements ClusteredLockService{

    @Override
    public Set<ClusteredLock> list(String category) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ClusteredLock lock(String category, String name, String owner, Duration autoUnlockTimeout) throws InterruptedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ClusteredLock tryLock(String category, String name, String owner, Duration autoUnlockTimeout, Duration waitingTime) throws InterruptedException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void unlock(ClusteredLock lock) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ClusteredLock getLock(String category, String name) {
        // TODO Auto-generated method stub
        return null;
    }

}
