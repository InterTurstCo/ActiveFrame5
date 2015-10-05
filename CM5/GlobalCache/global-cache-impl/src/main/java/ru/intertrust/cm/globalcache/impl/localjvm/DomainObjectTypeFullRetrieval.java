package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.dao.access.UserSubject;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Denis Mitavskiy
 *         Date: 20.08.2015
 *         Time: 15:09
 */
public class DomainObjectTypeFullRetrieval {
    private ConcurrentMap<Key, Boolean> systemTypeFullRetrieval;
    private ConcurrentMap<Key, ConcurrentMap<UserSubject, Boolean>> userTypeFullRetrieval;

    public DomainObjectTypeFullRetrieval(int objectsQty) {
        this.systemTypeFullRetrieval = new ConcurrentHashMap<>((int) (objectsQty / 0.75f + 1));
        this.userTypeFullRetrieval = new ConcurrentHashMap<>((int) (objectsQty / 0.75f + 1));
    }

    public void setTypeFullyRetrieved(String type, boolean exact, UserSubject subject, boolean value) {
        if (subject == null) {
            setTypeFullyRetrievedBySystem(type, exact, value);
        } else {
            setTypeFullyRetrievedByUser(type, exact, subject, value);
        }
    }

    private void setTypeFullyRetrievedBySystem(String type, boolean exact, boolean value) {
        systemTypeFullRetrieval.put(new Key(type, exact), value);
    }

    private void setTypeFullyRetrievedByUser(String type, boolean exact, UserSubject subject, boolean value) {
        final Key key = new Key(type, exact);
        ConcurrentMap<UserSubject, Boolean> byUser = userTypeFullRetrieval.get(key);
        if (byUser == null) {
            if (!value) {
                return;
            }
            final ConcurrentHashMap<UserSubject, Boolean> newMap = new ConcurrentHashMap<>();
            byUser = userTypeFullRetrieval.putIfAbsent(key, newMap);
            byUser = byUser == null ? newMap : byUser;
        }
        if (value) {
            byUser.put(subject, value);
        } else {
            byUser.remove(subject);
        }
    }

    public Boolean isTypeFullyRetrieved(String type, boolean exact, UserSubject subject) {
        if (subject == null) {
            return isTypeFullyRetrievedBySystem(type, exact);
        } else {
            return isTypeFullyRetrievedByUser(type, exact, subject);
        }
    }

    public Boolean isTypeFullyRetrievedBySystem(String type, boolean exact) {
        final Boolean result = systemTypeFullRetrieval.get(new Key(type, exact));
        return result == null ? Boolean.FALSE : result;
    }

    public Boolean isTypeFullyRetrievedByUser(String type, boolean exact, UserSubject subject) {
        final ConcurrentMap<UserSubject, Boolean> byUser = userTypeFullRetrieval.get(new Key(type, exact));
        if (byUser == null) {
            return Boolean.FALSE;
        }
        final Boolean result = byUser.get(subject);
        return result == null ? Boolean.FALSE : result;
    }

    public void clearTypeStatus(String type, boolean exact) {
        Key key = new Key(type, exact);
        systemTypeFullRetrieval.remove(key);
        userTypeFullRetrieval.remove(key);
    }

    public void clearTypeStatus(String type) {
        clearTypeStatus(type, true);
        clearTypeStatus(type, false);
    }

    public void clearTypeStatusForUser(UserSubject subject, String type, boolean exact) {
        final ConcurrentMap<UserSubject, Boolean> byUser = userTypeFullRetrieval.get(new Key(type, exact));
        if (byUser == null) {
            return;
        }
        byUser.remove(subject);
    }

    public void clearTypeStatusForUser(UserSubject subject, String type) {
        clearTypeStatusForUser(subject, type, true);
        clearTypeStatusForUser(subject, type, false);
    }

    private static final class Key {
        public final String type;
        public final boolean exact;

        public Key(String type, boolean exact) {
            this.type = type.toLowerCase();
            this.exact = exact;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (exact != key.exact) return false;
            if (!type.equals(key.type)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = type.hashCode();
            result = 31 * result + (exact ? 1 : 0);
            return result;
        }
    }
}
