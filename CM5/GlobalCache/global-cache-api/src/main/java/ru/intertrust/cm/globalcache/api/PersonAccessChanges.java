package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.*;

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
    private Set<Id> personsWhosAccessRightsRulesChanged = Collections.emptySet();
    private Set<Id> personsWhosAccessChanged = new HashSet<>();

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
        return personAccessByObject == null || personsWhosAccessRightsRulesChanged.size() > personsToClearFullAccessThreashold;
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
        personsWhosAccessChanged.addAll(personAccess.keySet());
        HashMap<Id, Boolean> currentPersonAccess = personAccessByObject.get(id);
        if (currentPersonAccess == null) {
            personAccessByObject.put(id, personAccess);
            totalRecordsQty += personAccess.size();
        } else {
            for (Map.Entry<Id, Boolean> newPersonAccessEntry : personAccess.entrySet()) {
                final Id personId = newPersonAccessEntry.getKey();
                final Boolean accessGranted = newPersonAccessEntry.getValue();
                if (currentPersonAccess.containsKey(personId)) {
                    final Boolean currentAccess = currentPersonAccess.get(personId);
                    if (Boolean.TRUE.equals(currentAccess)) { // person has access, nothing to change
                        continue;
                    }
                    if (Boolean.TRUE.equals(personAccess.get(personId))) {
                        currentPersonAccess.put(personId, Boolean.TRUE);
                    }
                } else {
                    currentPersonAccess.put(personId, accessGranted);
                    ++totalRecordsQty;
                }
            }
        }
        if (getObjectsQty() > objectsQtyThreashold || totalRecordsQty > totalRecordsThreashold) {
            markForFullAccessClearing();
        }
    }

    public HashMap<Id, HashMap<Id, Boolean>> getPersonAccessByObject() {
        return personAccessByObject;
    }

    public Set<Id> getPersonsWhosAccessChanged() {
        return personsWhosAccessChanged;
    }

    public Set<Id> getPersonsWhosAccessRightsRulesChanged() {
        return personsWhosAccessRightsRulesChanged;
    }

    public void setPersonsWhosAccessRightsRulesChanged(HashSet<Id> personsWhosAccessRightsRulesChanged) {
        this.personsWhosAccessRightsRulesChanged = personsWhosAccessRightsRulesChanged;
    }

    public boolean accessChangesExist() {
        return getObjectsQty() != 0 || !personsWhosAccessRightsRulesChanged.isEmpty() || personAccessByObject == null || !personAccessByObject.isEmpty();
    }

    private void markForFullAccessClearing() {
        personAccessByObject = null;
    }
}
