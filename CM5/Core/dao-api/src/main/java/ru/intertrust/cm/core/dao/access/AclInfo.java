package ru.intertrust.cm.core.dao.access;

import ru.intertrust.cm.core.business.api.dto.Id;

public class AclInfo {
    private AccessType accessType;
    private Id groupId;

    public AclInfo(AccessType accessType, Id groupId) {
        this.accessType = accessType;
        this.groupId = groupId;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public Id getGroupId() {
        return groupId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((accessType == null) ? 0 : accessType.hashCode());
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AclInfo other = (AclInfo) obj;
        if (accessType == null) {
            if (other.accessType != null)
                return false;
        } else if (!accessType.equals(other.accessType))
            return false;
        if (groupId == null) {
            if (other.groupId != null)
                return false;
        } else if (!groupId.equals(other.groupId))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();               
        result.append("\t\t\t{\n");
        result.append("\t\t\t\taccessType : " + accessType + "\n");
        result.append("\t\t\t\tgroupId : " + groupId + "\n");
        result.append("\t\t\t}\n");
        return result.toString();    
    }    
    
}
