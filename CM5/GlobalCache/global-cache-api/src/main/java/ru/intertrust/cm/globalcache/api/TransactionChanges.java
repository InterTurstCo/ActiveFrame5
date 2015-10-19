package ru.intertrust.cm.globalcache.api;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author Denis Mitavskiy
 *         Date: 10.08.2015
 *         Time: 15:31
 */
public class TransactionChanges {
    private GroupAccessChanges groupAccessChanges;
    private HashSet<Id> objectsChanged;
    private HashSet<String> typesChanged;

    public GroupAccessChanges getGroupAccessChanges() {
        return groupAccessChanges;
    }

    public void setGroupAccessChanges(GroupAccessChanges groupAccessChanges) {
        this.groupAccessChanges = groupAccessChanges;
    }

    public HashSet<String> getTypesChanged() {
        return typesChanged;
    }

    public void addObjectChanged(Id id, String type) {
        if (objectsChanged == null) {
            objectsChanged = new HashSet<>();
        }
        objectsChanged.add(id);
        addTypeChanged(type);
    }

    public boolean isObjectChanged(Id id) {
        return objectsChanged == null ? false : objectsChanged.contains(id);
    }

    public boolean isTypeSaved(String type) {
        return typesChanged == null ? false : typesChanged.contains(type.toLowerCase());
    }

    public boolean isAtLeastOneTypeSaved(Collection<String> types) {
        if (types == null) {
            return false;
        }
        for (String type : types) {
            if (typesChanged.contains(type.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void addTypeChanged(String type) {
        if (typesChanged == null) {
            typesChanged = new HashSet<>();
        }
        typesChanged.add(type.toLowerCase());
    }
}