package ru.intertrust.cm.core.business.api.dto.notification;

import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Класс представляющий адресата сообщения персону
 * @author larin
 * 
 */
public class NotificationAddresseePerson implements NotificationAddressee {
    private static final long serialVersionUID = 2097508626881933276L;
    private Id personId;

    public NotificationAddresseePerson() {
    }

    public NotificationAddresseePerson(Id personId) {
        this.personId = personId;
    }

    public Id getPersonId() {
        return personId;
    }

    public void setPersonId(Id personId) {
        this.personId = personId;
    }

    @Override
    public String toString() {
        return "NotificationAddresseePerson [personId=" + personId.toStringRepresentation() + "]";
    }
    
    
}
