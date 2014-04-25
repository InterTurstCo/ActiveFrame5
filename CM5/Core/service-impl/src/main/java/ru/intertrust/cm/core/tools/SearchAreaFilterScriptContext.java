package ru.intertrust.cm.core.tools;

import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;

/**
 * 
 * @author atsvetkov
 *
 */
public class SearchAreaFilterScriptContext extends BaseScriptContext {

    public SearchAreaFilterScriptContext(DomainObject domainObject) {
        super(domainObject);
    }

    public SearchAreaFilterScriptContext(Id id) {
        super(id);
    }

}
