package ru.intertrust.cm.core.gui.impl.server.action.access;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.access.AccessVerificationService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Created by andrey on 19.12.14.
 */
@ComponentName("default.delete.access.checker")
public class DefaultDeleteAccessChecker implements AccessChecker {
    @Autowired
    private AccessVerificationService accessVerificationService;

    @Override
    public boolean checkAccess(Id objectId) {
        return objectId != null && accessVerificationService.isDeletePermitted(objectId);
    }
}
