package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Denis Mitavskiy
 *         Date: 22.07.2015
 *         Time: 13:40
 */
public class PersonAccessChanges implements AccessChanges {
    private int totalRecordsQty;
    private int objectsQtyThreashold = 100000;
    private int totalRecordsThreashold = 1000000;

    private HashMap<Id, HashMap<Id, Boolean>> personAccessByObject;
    private HashSet<String> objectTypesAccessChanged = new HashSet<>();

    public PersonAccessChanges() {
        personAccessByObject = new HashMap<>();
    }

    public PersonAccessChanges(int initialObjectsQty, HashSet<String> objectTypesAccessChanged) {
        personAccessByObject = new HashMap<>((int) (initialObjectsQty / 0.75));
        this.objectTypesAccessChanged = objectTypesAccessChanged;
    }

    public PersonAccessChanges(boolean clearFullAccessLog, HashSet<String> objectTypesAccessChanged) {
        if (!clearFullAccessLog) {
            personAccessByObject = new HashMap<>();
        }
        this.objectTypesAccessChanged = objectTypesAccessChanged;
    }

    @Override
    public boolean clearFullAccessLog() {
        return personAccessByObject == null;
    }

    @Override
    public int getObjectsQty() {
        return personAccessByObject == null ? -1 : personAccessByObject.size();
    }

    @Override
    public HashSet<String> getObjectTypesAccessChanged() {
        return objectTypesAccessChanged;
    }

    public void addObjectPersonAccess(Id id, HashMap<Id, Boolean> personAccess) {
        if (clearFullAccessLog()) {
            return;
        }
        HashMap<Id, Boolean> previous = personAccessByObject.put(id, personAccess);
        totalRecordsQty += personAccess.size();
        if (previous != null) {
            totalRecordsQty -= previous.size();
        }
        if (getObjectsQty() > objectsQtyThreashold || totalRecordsQty > totalRecordsThreashold) {
            markForFullAccessClearing();
        }
    }

    public HashMap<Id, HashMap<Id, Boolean>> getPersonAccessByObject() {
        return personAccessByObject;
    }

    private void markForFullAccessClearing() {
        personAccessByObject = null;
    }
}
