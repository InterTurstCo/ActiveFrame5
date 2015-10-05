package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.UserSubject;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 10.07.2015
 *         Time: 13:51
 */
public class UserObjectAccess {
    private static final int INITAL_ACCESS_BY_USER_CAPACITY = 100;
    private static final int INITAL_ACCESS_BY_OBJECT_ID_CAPACITY = 1000;
    private static final int INITIAL_ACCESS_CAPACITY = 10000;
    private static final int INITIAL_OBJECTS_CAPACITY_PER_USER = 1000;
    private static final int INITIAL_USERS_CAPACITY_PER_OBJECT = 10;
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private ConcurrentHashMap<Record, Boolean> access;
    private ConcurrentHashMap<UserSubject, Set<Record>> accessByUser;
    private ConcurrentHashMap<Id, Set<Record>> accessByObjectId;

    public UserObjectAccess(int concurrencyLevel) {
        access = new ConcurrentHashMap<>(INITIAL_ACCESS_CAPACITY, DEFAULT_LOAD_FACTOR, concurrencyLevel);
        accessByUser = new ConcurrentHashMap<>(INITAL_ACCESS_BY_USER_CAPACITY, DEFAULT_LOAD_FACTOR, concurrencyLevel);
        accessByObjectId = new ConcurrentHashMap<>(INITAL_ACCESS_BY_OBJECT_ID_CAPACITY, DEFAULT_LOAD_FACTOR, concurrencyLevel);
    }

    public void clear() {

    }

    public void setAccess(UserSubject user, Id objectId, boolean accessGranted) {
        Record record = new Record(user, objectId);
        synchronized (record.getLock()) {
            if (access.containsKey(record)) {
                updateRecordAccess(record, accessGranted);
            } else {
                addRecord(record, accessGranted);
            }
        }
    }

    public void clearAccess(UserSubject user, Id objectId) {
        Record record = new Record(user, objectId);
        synchronized (record.getLock()) {
            if (access.containsKey(record)) {
                deleteRecord(record);
            }
        }
    }

    public void clearAccess(Id objectId) {
        final Set<Record> records = accessByObjectId.get(objectId);
        if (records == null) {
            return;
        }
        synchronized (records) { // just in case of 2 simultaneous removals
            if (accessByObjectId.get(objectId) != null) {
                for (Record record : records) {
                    access.remove(record);
                    final Set<Record> recordsByUser = accessByUser.get(record.subject);
                    if (recordsByUser != null) {
                        recordsByUser.remove(record);
                    }
                }
                accessByObjectId.remove(objectId);
            }
        }
    }

    public Boolean isAccessGranted(UserSubject user, Id objectId) {
        return access.get(new Record(user, objectId));
    }

    private void addRecord(Record record, Boolean accessGranted) {
        Set<Record> userAccess = getUserAccess(record.subject);
        Set<Record> objectUsers = getObjectUsers(record.objectId);

        access.put(record, accessGranted);
        userAccess.add(record);
        objectUsers.add(record);
    }

    private void updateRecordAccess(Record record, Boolean accessGranted) {
        access.put(record, accessGranted);
    }

    private void deleteRecord(Record record) {
        Set<Record> userAccess = getUserAccess(record.subject);
        Set<Record> objectUsers = getObjectUsers(record.objectId);

        access.remove(record);
        userAccess.remove(record);
        objectUsers.remove(record);
    }

    private Set<Record> getObjectUsers(Id objectId) {
        Set<Record> objectUsers = accessByObjectId.get(objectId);
        if (objectUsers == null) {
            synchronized (objectId.toStringRepresentation().intern()) {
                objectUsers = accessByObjectId.get(objectId);
                if (objectUsers == null) {
                    objectUsers = Collections.synchronizedSet(new HashSet<Record>(INITIAL_USERS_CAPACITY_PER_OBJECT));
                    accessByObjectId.put(objectId, objectUsers);
                }
            }
        }
        return objectUsers;
    }

    private Set<Record> getUserAccess(UserSubject user) {
        Set<Record> userAccess = accessByUser.get(user);
        if (userAccess == null) {
            synchronized (user.getName().intern()) {
                userAccess = accessByUser.get(user);
                if (userAccess == null) {
                    userAccess = Collections.synchronizedSet(new HashSet<Record>(INITIAL_OBJECTS_CAPACITY_PER_USER));
                    accessByUser.put(user, userAccess);
                }
            }
        }
        return userAccess;
    }

    private static final class Record {
        public final UserSubject subject;
        public final Id objectId;

        public Record(UserSubject subject, Id objectId) {
            this.subject = subject;
            this.objectId = objectId;
        }

        public String getLock() {
            return (subject.getName() + objectId.toStringRepresentation()).intern();
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
