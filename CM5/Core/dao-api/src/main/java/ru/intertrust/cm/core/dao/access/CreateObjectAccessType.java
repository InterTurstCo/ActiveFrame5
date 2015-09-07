package ru.intertrust.cm.core.dao.access;

import java.util.ArrayList;
import java.util.List;

/**
 * Тип доступа &mdash; создание доменного объекта заданного типа.
 * @author atsvetkov
 */
public class CreateObjectAccessType implements AccessType {

    private String objectType;
    private List<String> parentTypes;
    

    public CreateObjectAccessType(String objectType, List<String> parentTypes) {
        objectType.getClass(); // Just to throw NullPointerException
        this.objectType = objectType;
        this.parentTypes = parentTypes;
    }

    public String getObjectType() {
        return objectType;
    }
    
    public List<String> getParentTypes() {
        return parentTypes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((objectType == null) ? 0 : objectType.hashCode());

        if (parentTypes != null) {
            for (String parentType : parentTypes) {
                if (parentType != null) {
                    result = result + parentType.hashCode();
                }
            }
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CreateObjectAccessType other = (CreateObjectAccessType) obj;
        if (objectType == null) {
            if (other.objectType != null) {
                return false;
            }
        } else if (!objectType.equals(other.objectType)) {
            return false;
        }

        if (parentTypes == null) {
            if (other.parentTypes != null) {
                return false;
            }
        } else {
            if (other.parentTypes == null) {
                return false;
            } else {
                if (parentTypes.size() != other.parentTypes.size()) {
                    return false;
                }

                List<String> tempList = new ArrayList<>(parentTypes);
                tempList.removeAll(other.parentTypes);
                if (!tempList.isEmpty()) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "CreateObjectAccessType [objectType=" + objectType + ", parentTypes=" + parentTypes + "]";
    }
    
    
}
