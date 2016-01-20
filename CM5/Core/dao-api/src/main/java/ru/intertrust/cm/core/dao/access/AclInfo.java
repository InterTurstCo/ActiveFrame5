package ru.intertrust.cm.core.dao.access;

import ru.intertrust.cm.core.business.api.dto.Id;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AclInfo aclInfo = (AclInfo) o;
        return Objects.equals(accessType, aclInfo.accessType) &&
                Objects.equals(groupId, aclInfo.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accessType, groupId);
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
