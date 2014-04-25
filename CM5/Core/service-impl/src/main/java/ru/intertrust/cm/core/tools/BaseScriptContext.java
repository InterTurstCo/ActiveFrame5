package ru.intertrust.cm.core.tools;

import ru.intertrust.cm.core.business.api.ScriptContext;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * 
 * @author atsvetkov
 *
 */
public class BaseScriptContext extends DomainObjectAccessor implements ScriptContext {

    protected Boolean result;

    public BaseScriptContext(DomainObject domainObject) {
        super(domainObject);
    }

    public BaseScriptContext(Id id) {
        super(id);
    }

    @Override
    public Boolean getResult() {
        return result;
    }

    @Override
    public void setResult(Boolean result) {
        this.result = result;
    }

}
