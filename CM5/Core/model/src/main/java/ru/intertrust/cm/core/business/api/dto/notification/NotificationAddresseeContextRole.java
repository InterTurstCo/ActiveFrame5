package ru.intertrust.cm.core.business.api.dto.notification;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Класс определяющий адресата сообщения контекстную роль
 * @author larin
 * 
 */
public class NotificationAddresseeContextRole implements NotificationAddressee {
    private static final long serialVersionUID = -196530772745386049L;
    private Id contextId;
    private String roleName;

    public NotificationAddresseeContextRole() {
    }
    
    public NotificationAddresseeContextRole(String roleName, Id contextId) {
        this.contextId = contextId;
        this.roleName = roleName;
    }

    public Id getContextId() {
        return contextId;
    }

    public void setContextId(Id contextId) {
        this.contextId = contextId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

}
