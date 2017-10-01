package ru.intertrust.cm.globalcache.impl.localjvm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectsModification;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.impl.RdbmsId;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
import ru.intertrust.cm.globalcache.api.AccessChanges;
import ru.intertrust.cm.globalcache.api.PersonAccessChanges;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Collections.singleton;

/**
 * @author Denis Mitavskiy
 *         Date: 18.09.2017
 */
public class LockManager {
    private static final Logger logger = LoggerFactory.getLogger(LockManager.class);

    @Autowired
    private ConfigurationExplorer explorer;
    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;
    private NoLock noLock = new NoLock();
    private SingleReadLockImpl globalReadLock = new SingleReadLockImpl(new ReentrantReadWriteLock(true));
    private SingleWriteLockImpl globalWriteLock = new SingleWriteLockImpl(globalReadLock);
    private SingleReadLockImpl globalAccessReadLock = new SingleReadLockImpl(new ReentrantReadWriteLock(true));
    private SingleWriteLockImpl globalAccessWriteLock = new SingleWriteLockImpl(globalReadLock);
    private FixedLockTable<Integer> typeLocks = new FixedLockTable<>();
    private FixedLockTable<Integer> personLocks = new FixedLockTable<>();

    public GlobalCacheLock globalWriteLock() {
        return globalWriteLock.lock();
    }

    public GlobalCacheLock globalReadLock() {
        return globalReadLock.lock();
    }

    public GlobalCacheLock globalAccessWriteLock() {
        return globalAccessWriteLock.lock();
    }

    public GlobalCacheLock writeLock(Id id, DomainObject domainObject, UserSubject userSubject) {
        return buildLock(true, id, domainObject, userSubject).lock();
    }

    public GlobalCacheLock readLock(Id id, DomainObject domainObject, UserSubject userSubject) {
        return buildLock(false, id, domainObject, userSubject).lock();
    }

    public GlobalCacheLock writeLock(Collection<DomainObject> domainObjects, UserSubject userSubject) {
        return buildLock(true, domainObjects, userSubject).lock();
    }

    public GlobalCacheLock readLock(Collection<DomainObject> domainObjects, UserSubject userSubject) {
        return buildLock(false, domainObjects, userSubject).lock();
    }

    public GlobalCacheLock writeLockForPossiblyNullObjects(Collection<Pair<Id, DomainObject>> domainObjects, UserSubject userSubject) {
        HashSet<String> types = new HashSet<>();
        for (Pair<Id, DomainObject> domainObjectPair : domainObjects) {
            final DomainObject domainObject = domainObjectPair.getSecond();
            if (domainObject == null) {
                types.add(domainObjectTypeIdCache.getName(domainObjectPair.getFirst()));
            } else {
                types.add(domainObject.getTypeName());
            }
        }
        return buildLockAddingHierarchyAndDelegatingTypes(true, types, getPersonId(userSubject)).lock();
    }

    public GlobalCacheLock writeLock(String type, UserSubject userSubject) {
        return buildLockAddingHierarchyAndDelegatingTypes(true, singleton(type), getPersonId(userSubject)).lock();
    }

    public GlobalCacheLock readLock(String type, UserSubject userSubject) {
        return buildLockAddingHierarchyAndDelegatingTypes(false, singleton(type), getPersonId(userSubject)).lock();
    }

    public GlobalCacheLock writeLockTypes(Collection<String> types, UserSubject userSubject) {
        return buildLockAddingHierarchyAndDelegatingTypes(true, types, getPersonId(userSubject)).lock();
    }

    public GlobalCacheLock readLockTypes(Collection<String> types, UserSubject userSubject) {
        return buildLockAddingHierarchyAndDelegatingTypes(false, types, getPersonId(userSubject)).lock();
    }

    public GlobalCacheLockApi writeLock(Id id, String linkedType, UserSubject userSubject) {
        return buildLockAddingHierarchyAndDelegatingTypes(true, Arrays.asList(linkedType, domainObjectTypeIdCache.getName(id)), getPersonId(userSubject)).lock();
    }

    public GlobalCacheLockApi readLock(Id id, String linkedType, UserSubject userSubject) {
        return buildLockAddingHierarchyAndDelegatingTypes(false, Arrays.asList(linkedType, domainObjectTypeIdCache.getName(id)), getPersonId(userSubject)).lock();
    }

    public GlobalCacheLockApi readLockIds(Collection<Id> ids, UserSubject userSubject) {
        HashSet<String> types = new HashSet<>();
        for (Id id : ids) {
            types.add(domainObjectTypeIdCache.getName(id));
        }
        return buildLockAddingHierarchyAndDelegatingTypes(false, types, getPersonId(userSubject)).lock();
    }

    public GlobalCacheLock writeLock(DomainObjectsModification modification, AccessChanges accessChanges) {
        final int initialCapacity = modification.getCreatedDomainObjects().size() + modification.getSavedAndChangedStatusDomainObjects().size() + modification.getDeletedDomainObjects().size() + modification.getModifiedAutoDomainObjectIds().size();
        Set<String> types = initialCapacity == 0 ? Collections.<String>emptySet() : new HashSet<String>((int) (initialCapacity / 0.75f));
        for (DomainObject created : modification.getCreatedDomainObjects()) {
            types.add(created.getTypeName());
        }
        for (DomainObject updated : modification.getSavedAndChangedStatusDomainObjects()) {
            types.add(updated.getTypeName());
        }
        for (DomainObject deleted : modification.getDeletedDomainObjects()) {
            types.add(deleted.getTypeName());
        }
        for (Id automaticObjectId : modification.getModifiedAutoDomainObjectIds()) {
            types.add(domainObjectTypeIdCache.getName(automaticObjectId));
        }
        // findLinked(id, "file", "owner"); owner(type) == "*"
        // --> block type(id), "file" when finding linked - and everything will be fine - no one will access file (linked for type(id) ) when it's committed
        addHierarchyTypes(types);
        addTypesDelegatingAccess(types);


        final PersonAccessChanges personAccessChanges = (PersonAccessChanges) accessChanges;
        boolean clearFullAccessCache = false;
        Set<Integer> persons = Collections.emptySet();
        if (personAccessChanges.accessChangesExist()) {
            clearFullAccessCache = personAccessChanges.clearFullAccessLog();
            if (!clearFullAccessCache) {
                addTypesDelegatingAccess(types, accessChanges.getObjectTypesAccessChanged());
                persons = getPersonsIntegerIds(personAccessChanges.getPersonsWhosAccessChanged(), personAccessChanges.getPersonsWhosAccessRightsRulesChanged());
            }

        }
        return buildCommitLock(types, clearFullAccessCache, persons).lock();
    }

    public GlobalCacheLock writeLock(UserSubject userSubject) {
        return buildCommitLock(Collections.<String>emptySet(), false, getPersonId(userSubject));
    }

    private TreeSet<Integer> getPersonsIntegerIds(Collection<Id>... allPersonIds) {
        TreeSet<Integer> result = new TreeSet<>();
        for (Collection<Id> personIds : allPersonIds) {
            for (Id personId : personIds) {
                result.add((int) ((RdbmsId) personId).getId());
            }
        }
        return result;
    }

    private GlobalCacheLockApi buildLock(boolean writeLock, Id id, DomainObject domainObject, UserSubject userSubject) {
        if (domainObject != null) {
            return buildLockAddingHierarchyAndDelegatingTypes(writeLock, singleton(domainObject.getTypeName()), getPersonId(userSubject));
        } else {
            return buildLockAddingHierarchyAndDelegatingTypes(writeLock, singleton(domainObjectTypeIdCache.getName(id)), getPersonId(userSubject));
        }
    }

    private GlobalCacheLockApi buildLock(boolean writeLock, Collection<DomainObject> domainObjects, UserSubject userSubject) {
        HashSet<String> types = new HashSet<>();
        for (DomainObject domainObject : domainObjects) {
            types.add(domainObject.getTypeName());
        }
        return buildLockAddingHierarchyAndDelegatingTypes(writeLock, types, getPersonId(userSubject));
    }

    public void unlock(GlobalCacheLock globalCacheLock) {
        ((GlobalCacheLockApi) globalCacheLock).unlock();
    }

    public void unlockGlobalAccessWriteLock(GlobalCacheLock lock) {
        ((GenericLock) lock).unlockGlobalAccessWriteLock();
    }

    private static Set<Integer> getPersonId(UserSubject subject) {
        return subject == null ? Collections.<Integer>emptySet() : singleton(subject.getUserId());
    }

    private GlobalCacheLockApi buildCommitLock(Collection<String> types, boolean clearFullUserAccess, Set<Integer> personsIds) {
        try {
            if (types.isEmpty() && !clearFullUserAccess && personsIds.isEmpty()) {
                return noLock;
            }

            if (!personsIds.isEmpty() && !(personsIds instanceof TreeSet)) {
                personsIds = new TreeSet<>(personsIds);
            }
            GenericLock lock = new GenericLock(types.size() + personsIds.size() + 2);
            lock.add(globalReadLock.lock);
            lock.add(clearFullUserAccess ? globalAccessWriteLock.lock : globalAccessReadLock.lock);

            for (Integer typeId : getTypeIdsSorted(types)) {
                lock.add(this.typeLocks.getLock(typeId).writeLock());
            }
            for (Integer personId : personsIds) {
                lock.add(this.personLocks.getLock(personId).writeLock());
            }
            return lock;
        } catch (Throwable e) {
            logger.error("Exception while obtaining lock", e);
            return globalReadLock;
        }
    }

    private GlobalCacheLockApi buildLockAddingHierarchyAndDelegatingTypes(boolean typesWriteLock, Collection<String> types, Set<Integer> personsIds) {
        try {
            if (types.isEmpty() && personsIds.isEmpty()) {
                return noLock;
            }

            if (!personsIds.isEmpty() && !(personsIds instanceof TreeSet)) {
                personsIds = new TreeSet<>(personsIds);
            }
            // add hierarchy types, add types delgating access
            types = new HashSet<>(types);
            addHierarchyTypes(types);
            addTypesDelegatingAccess(types);

            GenericLock lock = new GenericLock(types.size() + personsIds.size() + 2);
            lock.add(globalReadLock.lock); // TODO: global lock should be always last, so that we have a chance to upgrade it to WRITE-lock
            lock.add(globalAccessReadLock.lock);
            if (typesWriteLock) {
                for (Integer typeId : getTypeIdsSorted(types)) {
                    lock.add(this.typeLocks.getLock(typeId).writeLock());
                }
            } else {
                for (Integer typeId : getTypeIdsSorted(types)) {
                    lock.add(this.typeLocks.getLock(typeId).readLock());
                }
            }
            for (Integer personId : personsIds) {
                lock.add(this.personLocks.getLock(personId).readLock());
            }
            return lock;
        } catch (Throwable e) {
            logger.error("Exception while obtaining lock", e);
            return globalReadLock;
        }
    }

    private void addTypesDelegatingAccess(Collection<String> types) {
        if (types.isEmpty()) {
            return;
        }
        Set<String> allDelegatingTypes = new HashSet<>();
        for (String typeAccessDelegatedTo : types) {
            final Set<String> delegatingTypes = explorer.getAllTypesDelegatingAccessCheckToInLowerCase(typeAccessDelegatedTo);
            addHierarchyTypes(allDelegatingTypes, delegatingTypes);
        }
        types.addAll(allDelegatingTypes);
    }

    private void addTypesDelegatingAccess(Set<String> addTo, Collection<String> typesAccessDelegatedTo) {
        if (addTo == typesAccessDelegatedTo) {
            addTypesDelegatingAccess(addTo);
        } else {
            for (String typeAccessDelegatedTo : typesAccessDelegatedTo) {
                final Set<String> delegatingTypes = explorer.getAllTypesDelegatingAccessCheckToInLowerCase(typeAccessDelegatedTo);
                addHierarchyTypes(addTo, delegatingTypes);
            }
        }
    }

    private void addHierarchyTypes(Collection<String> types) {
        if (!types.isEmpty()) {
            Set<String> extraTypesToLock = new HashSet<>();
            addHierarchyTypes(extraTypesToLock, types);
            types.addAll(extraTypesToLock);
        }
    }

    private void addHierarchyTypes(Set<String> addTo, Collection<String> types) {
        for (String objectType : types) {
            addTo.add(explorer.getDomainObjectRootType(objectType));
            // todo: it may be useful when more granular locing implemented
            // addTo.addAll(Arrays.asList(explorer.getDomainObjectTypesHierarchy(objectType)));
        }
    }

    private TreeSet<Integer> getTypeIdsSorted(Collection<String> types) {
        TreeSet<Integer> typeIds = new TreeSet<>();
        for (String type : types) {
            typeIds.add(domainObjectTypeIdCache.getId(type));
        }
        return typeIds;
    }

    public static interface GlobalCacheLock {

    }

    private interface GlobalCacheLockApi extends GlobalCacheLock {
        GlobalCacheLockApi lock();
        void unlock();
    }

    private class GenericLock extends ArrayList<Lock> implements GlobalCacheLockApi {
        private Lock globalAccessWriteLockCopy;

        public GenericLock(int initialCapacity) {
            super(initialCapacity);
        }

        public GenericLock() {
        }

        public GenericLock(Collection<? extends Lock> c) {
            super(c);
        }

        @Override
        public boolean add(Lock lock) {
            if (lock == globalAccessWriteLock.lock) {
                globalAccessWriteLockCopy = lock;
            }
            return super.add(lock);
        }

        @Override
        public GenericLock lock() {
            for (Lock lock : this) {
                lock.lock();
            }
            return this;
        }

        public void unlockGlobalAccessWriteLock() {
            if (globalAccessWriteLockCopy != null) {
                globalAccessWriteLockCopy.unlock();
                globalAccessWriteLockCopy = null;
                return;
            }
            throw new RuntimeException("Access write lock never obtained, but unlocked");
        }

        @Override
        public void unlock() {
            Lock lock;
            for (int i = size() - 1; i >= 0; --i) {
                lock = get(i);
                if (lock == globalAccessWriteLock.lock && globalAccessWriteLockCopy == null) {
                    continue;
                }
                lock.unlock();
            }
        }
    }

    private class SingleReadLockImpl implements GlobalCacheLockApi {
        private final ReentrantReadWriteLock.ReadLock lock;

        private SingleReadLockImpl(ReentrantReadWriteLock lock) {
            this.lock = lock.readLock();
        }

        private SingleReadLockImpl(SingleReadLockImpl from) {
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

    private class NoLock implements GlobalCacheLockApi {
        @Override
        public GlobalCacheLockApi lock() {
            return this;
        }

        @Override
        public void unlock() {
        }
    }

    private static class FixedLockTable<K> {
        private ConcurrentHashMap<K, ReentrantReadWriteLock> map = new ConcurrentHashMap<>(2048, 0.75f, 128);

        public ReentrantReadWriteLock getLock(K key) {
            ReentrantReadWriteLock lock = map.get(key);
            if (lock == null) {
                final ReentrantReadWriteLock newLock = new ReentrantReadWriteLock(true);
                final ReentrantReadWriteLock existing = map.putIfAbsent(key, newLock);
                lock = existing == null ? newLock : existing;
            }
            return lock;
        }
    }
}
