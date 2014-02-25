package ru.intertrust.cm.core.business.api.dto.notification;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Класс представляющий адресата сообщения персону
 * @author larin
 *
 */
public class NotificationAddresseePerson implements NotificationAddressee {
    private Id personId;

    public Id getPersonId() {
        return personId;
    }

    public void setPersonId(Id personId) {
        this.personId = personId;
    }
}
