package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AclInfo;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

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
    private HashSet<String> objectTypesAccessChanged = new HashSet<>();
    private HashSet<Id> personsWhosGroupsChanged = new HashSet<>();
    private HashSet<Id> groupsWithChangedBranching = new HashSet<>();
    private boolean groupsHierarchyChanged;

    public void aclCreated(Id contextObj, String type, Collection<AclInfo> recordsInserted) {
        aclInsertedOrDeleted(contextObj, recordsInserted, Boolean.TRUE);
        objectTypesAccessChanged.add(type);
    }

    public void aclDeleted(Id contextObj, String type, Collection<AclInfo> recordsInserted) {
        aclInsertedOrDeleted(contextObj, recordsInserted, Boolean.FALSE);
        objectTypesAccessChanged.add(type);
    }

    public void groupBranchChanged(Id groupId) {
        groupsWithChangedBranching.add(groupId);
    }

    public HashSet<Id> getGroupsWithChangedBranching() {
        return groupsWithChangedBranching;
    }

    public void personGroupChanged(Id personId) {
        personsWhosGroupsChanged.add(personId);
    }

    public HashSet<Id> getPersonsWhosGroupsChanged() {
        return personsWhosGroupsChanged;
    }

    public void setPersonsWhosGroupsChanged(HashSet<Id> personsWhosGroupsChanged) {
        this.personsWhosGroupsChanged = personsWhosGroupsChanged;
    }

    public boolean isGroupsHierarchyChanged() {
        return groupsHierarchyChanged;
    }

    public void setGroupsHierarchyChanged(boolean groupsHierarchyChanged) {
        this.groupsHierarchyChanged = groupsHierarchyChanged;
    }

    public boolean clearFullAccessLog() {
        return groupAccessByObject == null;
    }

    public boolean accessChangesExist() {
        return groupAccessByObject == null || groupAccessByObject.size() > 0 || !this.personsWhosGroupsChanged.isEmpty() || !this.groupsWithChangedBranching.isEmpty();
    }

    public int getObjectsQty() {
        return groupAccessByObject == null ? -1 : groupAccessByObject.size();
    }

    public HashMap<Id, HashMap<Id, Boolean>> getGroupAccessByObject() {
        return groupAccessByObject;
    }

    public HashSet<String> getObjectTypesAccessChanged() {
        return objectTypesAccessChanged;
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
