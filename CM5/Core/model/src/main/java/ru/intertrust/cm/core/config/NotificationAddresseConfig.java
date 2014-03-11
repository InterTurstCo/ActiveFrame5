package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Element;

import ru.intertrust.cm.core.business.api.dto.Dto;

/**
 * 
 * @author atsvetkov
 *
 */
public class NotificationAddresseConfig implements Dto {

    @Element(name = "find-person", required = false)
    private FindObjectsConfig findPerson;

    @Element(name = "by-context-role", required = false)
    private NotificationAddresseContextRoleConfig contextRole;

    @Element(name = "by-dynamic-group", required = false)
    private NotificationAddresseDynamicGroupConfig dynamicGroup;

    public FindObjectsConfig getFindPerson() {
        return findPerson;
    }

    public void setFindPerson(FindObjectsConfig findPerson) {
        this.findPerson = findPerson;
    }

    public NotificationAddresseContextRoleConfig getContextRole() {
        return contextRole;
    }

    public void setContextRole(NotificationAddresseContextRoleConfig contextRole) {
        this.contextRole = contextRole;
    }

    public NotificationAddresseDynamicGroupConfig getDynamicGroup() {
        return dynamicGroup;
    }

    public void setDynamicGroup(NotificationAddresseDynamicGroupConfig dynamicGroup) {
        this.dynamicGroup = dynamicGroup;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((contextRole == null) ? 0 : contextRole.hashCode());
        result = prime * result + ((dynamicGroup == null) ? 0 : dynamicGroup.hashCode());
        result = prime * result + ((findPerson == null) ? 0 : findPerson.hashCode());
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
        NotificationAddresseConfig other = (NotificationAddresseConfig) obj;
        if (contextRole == null) {
            if (other.contextRole != null) {
                return false;
            }
        } else if (!contextRole.equals(other.contextRole)) {
            return false;
        }
        if (dynamicGroup == null) {
            if (other.dynamicGroup != null) {
                return false;
            }
        } else if (!dynamicGroup.equals(other.dynamicGroup)) {
            return false;
        }
        if (findPerson == null) {
            if (other.findPerson != null) {
                return false;
            }
        } else if (!findPerson.equals(other.findPerson)) {
            return false;
        }
        return true;
    }    
}
