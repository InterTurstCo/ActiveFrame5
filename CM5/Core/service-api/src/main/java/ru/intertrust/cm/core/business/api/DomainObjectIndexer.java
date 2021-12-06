package ru.intertrust.cm.core.business.api;

import ru.intertrust.cm.core.business.api.dto.DomainObject;

public interface DomainObjectIndexer {

    void index(DomainObject domainObject);

}
