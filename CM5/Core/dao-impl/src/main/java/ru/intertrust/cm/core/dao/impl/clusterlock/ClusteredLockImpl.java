package ru.intertrust.cm.core.dao.impl.clusterlock;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
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
    private String stampInfo;

    public ClusteredLockImpl(){
    }

    public ClusteredLockImpl(String name, String category, boolean locked, String owner, Instant lockTime, Duration autoUnlockTimeout, String tag, String stampInfo) {
        this.name = name;
        this.category = category;
        this.locked = locked;
        this.owner = owner;
        this.lockTime = lockTime;
        this.autoUnlockTimeout = autoUnlockTimeout;
        this.tag = tag;
        this.stampInfo = stampInfo;
    }

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

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setLockTime(Instant lockTime) {
        this.lockTime = lockTime;
    }

    public void setAutoUnlockTimeout(Duration autoUnlockTimeout) {
        this.autoUnlockTimeout = autoUnlockTimeout;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getStampInfo() {
        return stampInfo;
    }

    public void setStampInfo(String stampInfo) {
        this.stampInfo = stampInfo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClusteredLockImpl that = (ClusteredLockImpl) o;
        return locked == that.locked &&
                Objects.equals(name, that.name) &&
                Objects.equals(category, that.category) &&
                Objects.equals(owner, that.owner) &&
                Objects.equals(lockTime, that.lockTime) &&
                Objects.equals(autoUnlockTimeout, that.autoUnlockTimeout) &&
                Objects.equals(stampInfo, that.stampInfo) &&
                Objects.equals(tag, that.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, category, locked, owner, lockTime, autoUnlockTimeout, tag, stampInfo);
    }
}
