package ru.intertrust.cm.core.service.it;

import java.util.List;

import javax.swing.event.DocumentEvent.EventType;

import ru.intertrust.cm.core.business.api.TriggerService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;

/**
 * 
 * @author atsvetkov
 *
 */
public class MyTestTrigger implements TriggerService {

    @Override
    public boolean isTriggered(String eventType, DomainObject domainObject, List<FieldModification> changedFields) {
        if (EventType.CHANGE.equals(eventType)) {
            return true;
        }
        return false;
    }

}
