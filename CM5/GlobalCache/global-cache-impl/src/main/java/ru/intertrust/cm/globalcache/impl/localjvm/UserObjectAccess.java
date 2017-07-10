package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;
import ru.intertrust.cm.globalcache.api.util.SizeableConcurrentHashMap;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 10.07.2015
 *         Time: 13:51
 */
public class UserObjectAccess implements Sizeable {
    private static final int INITAL_ACCESS_BY_USER_CAPACITY = 100;
    private static final int INITAL_ACCESS_BY_OBJECT_ID_CAPACITY = 1000;
    private static final int INITIAL_ACCESS_CAPACITY = 10000;
    private static final int INITIAL_OBJECTS_CAPACITY_PER_USER = 1000;
    private static final int INITIAL_USERS_CAPACITY_PER_OBJECT = 10;
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private Size size;

    private SizeableConcurrentHashMap<Record, Boolean> access;
    private SizeableConcurrentHashMap<UserSubject, SizeableConcurrentHashMap<Record, Record>> accessByUser;
    private SizeableConcurrentHashMap<Id, SizeableConcurrentHashMap<Record, Record>> accessByObjectId;

    public UserObjectAccess(int concurrencyLevel, Size sizeTotal) {
        size = new Size(sizeTotal);
        size.add(4 * SizeEstimator.REFERENCE_SIZE);

        // only duplicates and constants in this map - only self-size of map is calculated
        access = new SizeableConcurrentHashMap<>(INITIAL_ACCESS_CAPACITY, DEFAULT_LOAD_FACTOR, concurrencyLevel, size, false, false);

        // this map with submaps automatically takes nested objects into account
        accessByUser = new SizeableConcurrentHashMap<>(INITAL_ACCESS_BY_USER_CAPACITY, DEFAULT_LOAD_FACTOR, concurrencyLevel, size, true, true);

        // Ids are handled separately in this map
        accessByObjectId = new SizeableConcurrentHashMap<>(INITAL_ACCESS_BY_OBJECT_ID_CAPACITY, DEFAULT_LOAD_FACTOR, concurrencyLevel, size, true, false);
    }

    public void clear() {

    }

    public void setAccess(UserSubject user, Id objectId, Boolean accessGranted) {
        if (accessGranted == null) {
            clearAccess(user, objectId);
            return;
        }

        Record record = new Record(user, objectId);
        //synchronized (record.getLock()) { // todo - correct lock impl for async cache
        if (access.containsKey(record)) {
            updateRecordAccess(record, accessGranted);
        } else {
            addRecord(record, accessGranted);
        }
        //}
    }

    public void clearAccess(UserSubject user, Id objectId) {
        Record record = new Record(user, objectId);
        //synchronized (record.getLock()) { // todo - correct lock impl for async cache
        if (access.containsKey(record)) {
            deleteRecord(record);
        }
        //}
    }

    public void clearAccess(Id objectId) {
        final SizeableConcurrentHashMap<Record, Record> records = accessByObjectId.get(objectId);
        if (records == null) {
            return;
        }
        if (accessByObjectId.get(objectId) != null) {
            for (Record record : records.keySet()) {
                access.remove(record);
                final SizeableConcurrentHashMap<Record, Record> recordsByUser = accessByUser.get(record.subject);
                if (recordsByUser != null) {
                    recordsByUser.remove(record);
                }
            }
            accessByObjectId.remove(objectId);
        }
    }

    public void clearAccess(Collection<Id> objectIds) {
        if (objectIds == null) {
            return;
        }
        for (Id objectId : objectIds) {
            clearAccess(objectId);
        }
    }

    public Boolean isAccessGranted(UserSubject user, Id objectId) {
        return access.get(new Record(user, objectId));
    }

    private void addRecord(Record record, Boolean accessGranted) {
        SizeableConcurrentHashMap<Record, Record> userAccess = getUserAccess(record.subject);
        SizeableConcurrentHashMap<Record, Record> objectUsers = getObjectUsers(record.objectId);

        access.put(record, accessGranted);
        userAccess.put(record, record);
        objectUsers.put(record, record);
    }

    private void updateRecordAccess(Record record, Boolean accessGranted) {
        access.put(record, accessGranted);
    }

    private void deleteRecord(Record record) {
        SizeableConcurrentHashMap<Record, Record> userAccess = getUserAccess(record.subject);
        SizeableConcurrentHashMap<Record, Record> objectUsers = getObjectUsers(record.objectId);

        access.remove(record);
        userAccess.remove(record);
        objectUsers.remove(record);
    }

    private SizeableConcurrentHashMap<Record, Record> getObjectUsers(Id objectId) {
        SizeableConcurrentHashMap<Record, Record> objectUsers = accessByObjectId.get(objectId);
        if (objectUsers == null) {
            // synchronized (objectId.toStringRepresentation().intern()) { // todo - correct lock impl for async cache
            objectUsers = accessByObjectId.get(objectId);
            if (objectUsers == null) {
                objectUsers = new SizeableConcurrentHashMap<>(INITIAL_USERS_CAPACITY_PER_OBJECT, DEFAULT_LOAD_FACTOR, 16, null, false, false);
                accessByObjectId.put(objectId, objectUsers);
            }
            // }
        }
        return objectUsers;
    }

    private SizeableConcurrentHashMap<Record, Record> getUserAccess(UserSubject user) {
        SizeableConcurrentHashMap<Record, Record> userAccess = accessByUser.get(user);
        if (userAccess == null) {
            // synchronized (user.getName().intern()) { // todo - correct lock impl for async cache
                userAccess = accessByUser.get(user);
                if (userAccess == null) {
                    userAccess = newUserAccessMap();
                    accessByUser.put(user, userAccess);
                }
            // }
        }
        return userAccess;
    }

    public void clearAccess(UserSubject user) {
        accessByUser.put(user, newUserAccessMap());
    }

    private SizeableConcurrentHashMap<Record, Record> newUserAccessMap() {
        return new SizeableConcurrentHashMap<>(INITIAL_OBJECTS_CAPACITY_PER_USER, DEFAULT_LOAD_FACTOR, 16, null, true, true);
    }

    @Override
    public Size getSize() {
        return size;
    }

    private static final class Record {
        public final UserSubject subject;
        public final Id objectId;

        public Record(UserSubject subject, Id objectId) {
            this.subject = subject;
            this.objectId = objectId;
        }

        public String getLock() {
            return ""; //(subject.getName() + objectId.toStringRepresentation()).intern(); // todo - correct lock impl for async cache
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Record that = (Record) o;

            if (!subject.equals(that.subject)) {
                return false;
            }
            if (!objectId.equals(that.objectId)) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = subject.hashCode();
            result = 31 * result + objectId.hashCode();
            return result;
        }
    }

    private static final class ConcurrentHashSet<T> extends AbstractSet<T> {
        private static final Object VALUE = new Object();

        private ConcurrentHashMap<T, Object> map;

        public ConcurrentHashSet() {
            map = new ConcurrentHashMap<>();
        }

        public ConcurrentHashSet(Collection<? extends T> c) {
            map = new ConcurrentHashMap<>(Math.max((int) (c.size() / .75f) + 1, 16));
            addAll(c);
        }

        public ConcurrentHashSet(Collection<? extends T> c, int concurrencyLevel) {
            map = new ConcurrentHashMap<>(Math.max((int) (c.size() / .75f) + 1, 16), 0.75f, concurrencyLevel);
            addAll(c);
        }

        public ConcurrentHashSet(int initialCapacity, float loadFactor) {
            map = new ConcurrentHashMap<>(initialCapacity, loadFactor);
        }

        public ConcurrentHashSet(int initialCapacity, float loadFactor, int concurrencyLevel) {
            map = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
        }

        public ConcurrentHashSet(int initialCapacity) {
            map = new ConcurrentHashMap<>(initialCapacity);
        }

        public Iterator<T> iterator() {
            return map.keySet().iterator();
        }

        public int size() {
            return map.size();
        }

        public boolean isEmpty() {
            return map.isEmpty();
        }

        public boolean contains(Object o) {
            return map.containsKey(o);
        }

        public boolean add(T e) {
            return map.put(e, VALUE) == null;
        }

        public boolean remove(Object o) {
            return map.remove(o) == VALUE;
        }

        public void clear() {
            map.clear();
        }

    }
}
