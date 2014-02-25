package ru.intertrust.cm.core.business.api.dto.notification;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Класс определяющий адресата сообщения контекстную роль
 * @author larin
 * 
 */
public class NotificationAddresseeContextRole implements NotificationAddressee {
    private Id contextId;
    private String roleName;

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
