package ru.intertrust.cm.core.dao.impl.access;

import org.springframework.stereotype.Service;
import ru.intertrust.cm.core.config.BaseOperationPermitConfig;
import ru.intertrust.cm.core.config.CreateChildConfig;
import ru.intertrust.cm.core.config.DeleteConfig;
import ru.intertrust.cm.core.config.ExecuteActionConfig;
import ru.intertrust.cm.core.config.ReadAttachmentConfig;
import ru.intertrust.cm.core.config.ReadConfig;
import ru.intertrust.cm.core.config.WriteConfig;
import ru.intertrust.cm.core.dao.access.AccessType;
import ru.intertrust.cm.core.dao.access.CreateChildAccessType;
import ru.intertrust.cm.core.dao.access.DomainObjectAccessType;
import ru.intertrust.cm.core.dao.access.ExecuteActionAccessType;

@Service
public class AccessControlConverterImpl implements AccessControlConverter {

    @Override
    public AccessType codeToAccessType(String operation) {
        AccessType accessType = null;
        if (operation.equals(OP_READ)) {
            accessType = DomainObjectAccessType.READ;
        } else if (operation.equals(OP_WRITE)) {
            accessType = DomainObjectAccessType.WRITE;
        } else if (operation.equals(OP_DELETE)) {
            accessType = DomainObjectAccessType.DELETE;
        } else if (operation.equals(OP_READ_ATTACH)) {
            accessType = DomainObjectAccessType.READ_ATTACH;
        } else if (operation.startsWith(OP_EXEC_ACTION_PREF)) {
            accessType = new ExecuteActionAccessType(operation.substring(2));
        } else if (operation.startsWith(OP_CREATE_CHILD_PREF)) {
            accessType = new CreateChildAccessType(operation.substring(2));
        }
        return accessType;
    }

    @Override
    public String accessTypeToCode(AccessType type) {

        if (DomainObjectAccessType.READ.equals(type)) {
            return OP_READ;
        }
        if (DomainObjectAccessType.WRITE.equals(type)) {
            return OP_WRITE;
        }
        if (DomainObjectAccessType.DELETE.equals(type)) {
            return OP_DELETE;
        }
        if (DomainObjectAccessType.READ_ATTACH.equals(type)) {
            return OP_READ_ATTACH;
        }
        if (CreateChildAccessType.class.equals(type.getClass())) {
            CreateChildAccessType ccType = (CreateChildAccessType) type;
            return new StringBuilder(OP_CREATE_CHILD_PREF).append(ccType.getChildType()).toString();
        }
        if (ExecuteActionAccessType.class.equals(type.getClass())) {
            ExecuteActionAccessType executeActionType = (ExecuteActionAccessType) type;
            return new StringBuilder(OP_EXEC_ACTION_PREF).append(executeActionType.getActionName()).toString();
        }

        return null;
    }

    @Override
    public AccessType configToAccessType(BaseOperationPermitConfig operationPermitConfig) {
        AccessType accessType = null;
        if (operationPermitConfig.getClass().equals(ReadConfig.class)) {
            accessType = DomainObjectAccessType.READ;
        } else if (operationPermitConfig.getClass().equals(WriteConfig.class)) {
            accessType = DomainObjectAccessType.WRITE;
        } else if (operationPermitConfig.getClass().equals(DeleteConfig.class)) {
            accessType = DomainObjectAccessType.DELETE;
        } else if (operationPermitConfig.getClass().equals(ReadAttachmentConfig.class)) {
            accessType = DomainObjectAccessType.READ_ATTACH;
        } else if (operationPermitConfig.getClass().equals(ExecuteActionConfig.class)) {
            String actionName = ((ExecuteActionConfig) operationPermitConfig).getName();
            accessType = new ExecuteActionAccessType(actionName);
        } else if (operationPermitConfig.getClass().equals(CreateChildConfig.class)) {
            String childType = ((CreateChildConfig) operationPermitConfig).getType();
            accessType = new CreateChildAccessType(childType);
        }

        return accessType;
    }
}
