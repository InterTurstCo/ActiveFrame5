package ru.intertrust.cm.core.gui.impl.server.action.access;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Created by andrey on 19.12.14.
 */
@ComponentName("default.delete.access.checker")
public class DefaultDeleteAccessChecker implements AccessChecker {
    @Override
    public boolean checkAccess(Id objectId) {
        System.out.println(this.getClass() + " access checked");
        return true;
    }
}
