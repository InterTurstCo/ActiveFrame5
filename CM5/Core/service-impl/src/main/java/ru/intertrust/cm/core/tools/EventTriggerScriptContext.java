package ru.intertrust.cm.core.tools;

import java.util.List;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Контекст, передаваемый в скрипт. Содержит измененный доменный объект, тип события, список измененных полей, а также
 * результат вычисления скриптового выражения.
 * @author atsvetkov
 */
public class EventTriggerScriptContext extends BaseScriptContext {

    private String eventType;
    
    private List<FieldModification> changedFields;
    
    public EventTriggerScriptContext(DomainObject domainObject) {
        super(domainObject);
    }

    public EventTriggerScriptContext(Id id) {
        super(id);
    }

    public EventTriggerScriptContext(DomainObject domainObject, String eventType) {
        super(domainObject);
        this.eventType = eventType;
    }
    
    public EventTriggerScriptContext(DomainObject domainObject, String eventType, List<FieldModification> changedFields) {
        super(domainObject);
        this.eventType = eventType;
        this.changedFields = changedFields;
    }

    public List<FieldModification> getChangedFields() {
        return changedFields;
    }        
}
