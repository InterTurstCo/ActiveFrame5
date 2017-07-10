package ru.intertrust.cm.globalcache.impl.localjvm;

import ru.intertrust.cm.core.dao.access.UserSubject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Denis Mitavskiy
 *         Date: 04.07.2017
 *         Time: 19:06
 */
public class UserAccessChangeTime {
    private ConcurrentHashMap<UserSubject, ModificationTime> accessChangeTime;

    public UserAccessChangeTime(int usersQty) {
        this.accessChangeTime = new ConcurrentHashMap<>((int) (usersQty / 0.75 + 1));
    }

    public void setLastRightsChangeTime(UserSubject user, long rightsChangeTime) {
        ModificationTime previousTime = accessChangeTime.get(user);
        if (previousTime == null) {
            final ModificationTime putResult = accessChangeTime.putIfAbsent(user, new ModificationTime(-1, -1, rightsChangeTime));
            if (putResult == null) {
                return;
            }
            previousTime = putResult;
        }
        previousTime.updateRightsChangeTime(rightsChangeTime);
    }

    public void setLastRightsChangeTimeForEveryone(long rightsChangeTime) {
        for (UserSubject userSubject : accessChangeTime.keySet()) {
            setLastRightsChangeTime(userSubject, rightsChangeTime);
        }
    }

    public ModificationTime getLastAccessModificationTime(UserSubject user) {
        return accessChangeTime.get(user);
    }
}
