package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectsModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.globalcache.api.AccessChanges;

import java.util.Collection;

public interface LockManager {
    GlobalCacheLock globalWriteLock();

    GlobalCacheLock globalReadLock();

    GlobalCacheLock globalAccessWriteLock();

    GlobalCacheLock writeLock(Id id, DomainObject domainObject, UserSubject userSubject);

    GlobalCacheLock readLock(Id id, DomainObject domainObject, UserSubject userSubject);

    GlobalCacheLock writeLock(Collection<DomainObject> domainObjects, UserSubject userSubject);

    GlobalCacheLock writeLockForPossiblyNullObjects(Collection<Pair<Id, DomainObject>> domainObjects, UserSubject userSubject);

    GlobalCacheLock writeLock(String type, UserSubject userSubject);

    GlobalCacheLock readLock(String type, UserSubject userSubject);

    GlobalCacheLock writeLockTypes(Collection<String> types, UserSubject userSubject);

    GlobalCacheLock readLockTypes(Collection<String> types, UserSubject userSubject);

    GlobalCacheLockApi writeLock(Id id, String linkedType, UserSubject userSubject);

    GlobalCacheLockApi readLock(Id id, String linkedType, UserSubject userSubject);

    GlobalCacheLockApi readLockIds(Collection<Id> ids, UserSubject userSubject);

    GlobalCacheLock writeLock(DomainObjectsModification modification, AccessChanges accessChanges);

    GlobalCacheLock writeLock(UserSubject userSubject);

    void unlock(GlobalCacheLock globalCacheLock);

    void unlockGlobalAccessWriteLock(GlobalCacheLock lock);
}
