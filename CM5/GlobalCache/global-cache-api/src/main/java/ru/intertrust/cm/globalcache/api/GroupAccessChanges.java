package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AclInfo;

import java.util.Collection;
import java.util.HashMap;

/**
 * Потоко-безопасный класс, так как все действия выполняются в пределах одной транзакции
 * @author Denis Mitavskiy
 *         Date: 21.07.2015
 *         Time: 19:19
 */
public class GroupAccessChanges implements AccessChanges {
    private int totalRecordsQty;
    private int objectsQtyThreashold = 100000;
    private int totalRecordsThreashold = 1000000;

    // key is Domain Object ID, key of inner map - ID of a group
    private HashMap<Id, HashMap<Id, Boolean>> groupAccessByObject = new HashMap<>();

    public void aclCreated(Id contextObj, Collection<AclInfo> recordsInserted) {
        aclInsertedOrDeleted(contextObj, recordsInserted, Boolean.TRUE);
    }

    public void aclDeleted(Id contextObj, Collection<AclInfo> recordsInserted) {
        aclInsertedOrDeleted(contextObj, recordsInserted, Boolean.FALSE);
    }

    public boolean clearFullAccessLog() {
        return groupAccessByObject == null;
    }

    public boolean accessChangesExist() {
        return groupAccessByObject == null || groupAccessByObject.size() > 0;
    }

    public int getObjectsQty() {
        return groupAccessByObject == null ? -1 : groupAccessByObject.size();
    }

    public HashMap<Id, HashMap<Id, Boolean>> getGroupAccessByObject() {
        return groupAccessByObject;
    }

    private void aclInsertedOrDeleted(Id contextObj, Collection<AclInfo> records, Boolean accessGranted) {
        if (clearFullAccessLog()) {
            return;
        }
        HashMap<Id, Boolean> groupAccess = getGroupAccess(contextObj);
        if (getObjectsQty() > objectsQtyThreashold) {
            markForFullAccessClearing();
            return;
        }
        for (AclInfo aclInfo : records) {
            Boolean previous = groupAccess.put(aclInfo.getGroupId(), accessGranted);
            if (previous == null) {
                ++totalRecordsQty;
            }
        }
        if (totalRecordsQty > totalRecordsThreashold) {
            markForFullAccessClearing();
        }
    }

    private HashMap<Id, Boolean> getGroupAccess(Id objectId) {
        HashMap<Id, Boolean> groupAccess = groupAccessByObject.get(objectId);
        if (groupAccess != null) {
            return groupAccess;
        }
        groupAccess = new HashMap<>();
        groupAccessByObject.put(objectId, groupAccess);
        return groupAccess;
    }

    private void markForFullAccessClearing() {
        groupAccessByObject = null;
    }
}
