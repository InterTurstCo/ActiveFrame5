package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.dao.access.UserSubject;
import ru.intertrust.cm.globalcache.api.util.Size;
import ru.intertrust.cm.globalcache.api.util.SizeEstimator;
import ru.intertrust.cm.globalcache.api.util.Sizeable;
import ru.intertrust.cm.globalcache.api.util.SizeableConcurrentHashMap;

import java.util.concurrent.ConcurrentMap;

/**
 * @author Denis Mitavskiy
 *         Date: 20.08.2015
 *         Time: 15:09
 */
public class DomainObjectTypeFullRetrieval implements Sizeable {
    private SizeableConcurrentHashMap<Key, Boolean> systemTypeFullRetrieval;
    private SizeableConcurrentHashMap<Key, SizeableConcurrentHashMap<UserSubject, Boolean>> userTypeFullRetrieval;

    private Size size;

    public DomainObjectTypeFullRetrieval(int objectsQty, Size cacheSizeTotal) {
        this.size = new Size(cacheSizeTotal);
        this.size.set(2 * SizeEstimator.REFERENCE_SIZE);
        this.systemTypeFullRetrieval = new SizeableConcurrentHashMap<>((int) (objectsQty / 0.75f + 1), 0.75f, 16, size, true, false);
        this.userTypeFullRetrieval = new SizeableConcurrentHashMap<>((int) (objectsQty / 0.75f + 1), 0.75f, 16, size, true, true);
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
        SizeableConcurrentHashMap<UserSubject, Boolean> byUser = userTypeFullRetrieval.get(key);
        if (byUser == null) {
            if (!value) {
                return;
            }
            final SizeableConcurrentHashMap<UserSubject, Boolean> newMap = new SizeableConcurrentHashMap<>(128, 0.75f, 16, true, false);
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

    public void clearAllUsersFullRetrievalInfo() {
        userTypeFullRetrieval.getSize().detachFromTotal();
        userTypeFullRetrieval = new SizeableConcurrentHashMap<>(userTypeFullRetrieval.size(), 0.75f, 16, size, true, true);
    }

    public void clearTypeStatus(String type, boolean exact) {
        Key key = new Key(type, exact);
        systemTypeFullRetrieval.remove(key);
        userTypeFullRetrieval.remove(key);
    }

    /**
     * Clear status
     * @param type exact type
     */
    public void clearUsersTypeStatus(String type, Boolean exact) {
        if (exact == null) {
            userTypeFullRetrieval.remove(new Key(type, true));
            userTypeFullRetrieval.remove(new Key(type, false));
        } else {
            userTypeFullRetrieval.remove(new Key(type, exact));
        }
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

    public void clearAllTypesForUser(UserSubject subject) {
        for (SizeableConcurrentHashMap<UserSubject, Boolean> byUser : userTypeFullRetrieval.values()) {
            byUser.remove(subject);
        }
    }

    public void clearTypeStatusForUser(UserSubject subject, String type) {
        clearTypeStatusForUser(subject, type, true);
        clearTypeStatusForUser(subject, type, false);
    }

    @Override
    public Size getSize() {
        return size;
    }

    private static final class Key {
        public final String type;
        public final boolean exact;

        public Key(String type, boolean exact) {
            this.type = Case.toLower(type);
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
