package ru.intertrust.cm.core.dao.impl.access;

import ru.intertrust.cm.core.config.BaseOperationPermitConfig;
import ru.intertrust.cm.core.dao.access.AccessType;

public interface AccessControlConverter {

    String OP_DELIM = "_";
    String OP_READ = "R";
    String OP_WRITE = "W";
    String OP_DELETE = "D";
    String OP_READ_ATTACH = "RA";
    String OP_EXEC_ACTION = "E";
    String OP_EXEC_ACTION_PREF = OP_EXEC_ACTION + OP_DELIM;
    String OP_CREATE_CHILD = "C";
    String OP_CREATE_CHILD_PREF = OP_CREATE_CHILD + OP_DELIM;

    AccessType codeToAccessType(String operation);

    String accessTypeToCode(AccessType accessType);

    AccessType configToAccessType(BaseOperationPermitConfig operationPermitConfig);
}
