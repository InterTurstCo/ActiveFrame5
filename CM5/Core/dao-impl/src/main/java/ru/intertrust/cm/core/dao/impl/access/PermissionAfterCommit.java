package ru.intertrust.cm.core.dao.impl.access;

import java.util.Map;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.dao.access.AclData;

public interface PermissionAfterCommit {
    void onAfterCommit(Map<Id, AclData> aclDatas);
}
