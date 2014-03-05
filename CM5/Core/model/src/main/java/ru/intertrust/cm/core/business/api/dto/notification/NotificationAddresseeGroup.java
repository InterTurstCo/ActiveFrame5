package ru.intertrust.cm.core.business.api.dto.notification;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Класс определяющий адресата сообщения группу
 * @author larin
 * 
 */
public class NotificationAddresseeGroup implements NotificationAddressee {

    private static final long serialVersionUID = 4161734555831220548L;

    private Id groupId;

    public NotificationAddresseeGroup() {
    }

    public NotificationAddresseeGroup(Id groupId) {
        this.groupId = groupId;
    }

    public Id getGroupId() {
        return groupId;
    }

    public void setGroupId(Id groupId) {
        this.groupId = groupId;
    }

}
