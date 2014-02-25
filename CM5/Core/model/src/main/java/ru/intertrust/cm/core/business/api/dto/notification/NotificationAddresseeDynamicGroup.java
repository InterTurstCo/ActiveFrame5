package ru.intertrust.cm.core.business.api.dto.notification;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Класс определяющий адресата сообщения динамическую группу
 * @author larin
 * 
 */
public class NotificationAddresseeDynamicGroup implements NotificationAddressee {
    private Id contextId;
    private String groupName;

    public Id getContextId() {
        return contextId;
    }

    public void setContextId(Id contextId) {
        this.contextId = contextId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

}
