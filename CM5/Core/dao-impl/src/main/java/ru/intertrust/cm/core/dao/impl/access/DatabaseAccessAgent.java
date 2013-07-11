package ru.intertrust.cm.core.dao.impl.access;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AccessType;

public interface DatabaseAccessAgent {

    boolean checkDomainObjectAccess(int userId, Id objectId, AccessType type);

    boolean checkMultiDomainObjectAccess(int userId, Id[] objectIds, AccessType type);

    boolean checkDomainObjectMultiAccess(int userId, Id objectId, AccessType[] types);
}
