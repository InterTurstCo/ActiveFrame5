package ru.intertrust.cm.core.dao.impl.clusterlock;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import ru.intertrust.cm.core.dao.api.clusterlock.ClusteredLock;

public class ClusteredLockImpl implements ClusteredLock{
    private String name; 
    private String category; 
    private boolean locked; 
    private String owner; 
    private Instant lockTime; 
    private Duration autoUnlockTimeout; 
    private String tag; 

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public String getOwner() {
        return owner;
    }

    @Override
    public Optional<Instant> getLockTime() {
        return Optional.ofNullable(lockTime);
    }

    @Override
    public Duration getAutoUnlockTimeout() {
        return autoUnlockTimeout;
    }

    @Override
    public String getTag() {
        return tag;
    }

}
