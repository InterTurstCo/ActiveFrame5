package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Denis Mitavskiy
 *         Date: 22.07.2015
 *         Time: 13:40
 */
public class PersonAccessChanges implements AccessChanges {
    private int totalRecordsQty;
    private int objectsQtyThreashold = 100000;
    private int totalRecordsThreashold = 1000000;
    private int personsToClearFullAccessThreashold = 100;

    private HashMap<Id, HashMap<Id, Boolean>> personAccessByObject;
    private HashSet<String> objectTypesAccessChanged = new HashSet<>();
    private Set<Id> personsWhosAccessRightsChanged = Collections.emptySet();

    public PersonAccessChanges() {
        personAccessByObject = new HashMap<>();
    }

    public PersonAccessChanges(int initialObjectsQty, HashSet<String> objectTypesAccessChanged) {
        personAccessByObject = initialObjectsQty < 16 ? new HashMap<Id, HashMap<Id, Boolean>>() : new HashMap<Id, HashMap<Id, Boolean>>((int) (initialObjectsQty / 0.75));
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
        return personAccessByObject == null || personsWhosAccessRightsChanged.size() > personsToClearFullAccessThreashold;
    }

    @Override
    public int getObjectsQty() {
        return clearFullAccessLog() ? -1 : personAccessByObject.size();
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

    public Set<Id> getPersonsWhosAccessRightsChanged() {
        return personsWhosAccessRightsChanged;
    }

    public void setPersonsWhosAccessRightsChanged(HashSet<Id> personsWhosAccessRightsChanged) {
        this.personsWhosAccessRightsChanged = personsWhosAccessRightsChanged;
    }

    public boolean accessChangesExist() {
        return getObjectsQty() != 0 || !personsWhosAccessRightsChanged.isEmpty() || personAccessByObject == null || !personAccessByObject.isEmpty();
    }

    private void markForFullAccessClearing() {
        personAccessByObject = null;
    }
}
