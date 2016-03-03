package ru.intertrust.cm.core.business.impl.notification;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.impl.EventType;

import java.util.List;

/**
* @author Denis Mitavskiy
*         Date: 19.12.2014
*         Time: 15:56
*/
public class SendNotificationInfo implements Dto {
    private DomainObject domainObject;
    private EventType eventType;
    private List<FieldModification> changedFields;

    public DomainObject getDomainObject() {
        return domainObject;
    }

    public void setDomainObject(DomainObject domainObject) {
        this.domainObject = domainObject;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public List<FieldModification> getChangedFields() {
        return changedFields;
    }

    public void setChangedFields(List<FieldModification> changedFields) {
        this.changedFields = changedFields;
    }
}
