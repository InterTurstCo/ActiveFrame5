package ru.intertrust.cm.core.business.api.dto.notification;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Класс определяющий адресата сообщения динамическую группу
 * @author larin
 * 
 */
public class NotificationAddresseeDynamicGroup implements NotificationAddressee {
    private static final long serialVersionUID = 4351558596976291933L;
    private Id contextId;
    private String groupName;

    public NotificationAddresseeDynamicGroup() {
    }
    
    public NotificationAddresseeDynamicGroup(String groupName, Id contextId) {
        this.contextId = contextId;
        this.groupName = groupName;
    }

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
