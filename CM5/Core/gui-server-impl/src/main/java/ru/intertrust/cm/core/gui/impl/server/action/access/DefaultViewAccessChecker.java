package ru.intertrust.cm.core.gui.impl.server.action.access;

import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.access.AccessVerificationService;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * @author Yaroslav Bondarchuk
 *         Date: 05.03.2015
 *         Time: 7:58
 */
@ComponentName("default.view.access.checker")
public class DefaultViewAccessChecker implements AccessChecker{
    @Autowired
    private AccessVerificationService accessVerificationService;

    @Override
    public boolean checkAccess(Id objectId) {
        return objectId != null && accessVerificationService.isReadPermitted(objectId);
    }
}
