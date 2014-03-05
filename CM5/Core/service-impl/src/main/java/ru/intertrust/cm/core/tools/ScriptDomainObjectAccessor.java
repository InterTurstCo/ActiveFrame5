package ru.intertrust.cm.core.tools;

import java.util.List;

import ru.intertrust.cm.core.business.api.ScriptContext;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.FieldModification;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * Контекст, передаваемый в скрипт. Содержит измененный доменный объект, тип события, список измененных полей, а также
 * результат вычисления скриптового выражения.
 * @author atsvetkov
 */
public class ScriptDomainObjectAccessor extends DomainObjectAccessor implements ScriptContext {

    private Object result;
    
    private String eventType;
    
    private List<FieldModification> changedFields;
    
    public ScriptDomainObjectAccessor(DomainObject domainObject) {
        super(domainObject);
    }

    public ScriptDomainObjectAccessor(DomainObject domainObject, String eventType) {
        super(domainObject);
        this.eventType = eventType;
    }
    
    public ScriptDomainObjectAccessor(DomainObject domainObject, String eventType, List<FieldModification> changedFields) {
        super(domainObject);
        this.eventType = eventType;
        this.changedFields = changedFields;
    }

    public ScriptDomainObjectAccessor(Id id) {
        super(id);
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public List<FieldModification> getChangedFields() {
        return changedFields;
    }        
}
