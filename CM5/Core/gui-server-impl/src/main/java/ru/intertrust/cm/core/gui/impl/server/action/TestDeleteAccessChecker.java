package ru.intertrust.cm.core.gui.impl.server.action;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.impl.server.action.access.AccessChecker;
import ru.intertrust.cm.core.gui.model.ComponentName;

/**
 * Created by ravil on 23.10.17.
 */
@ComponentName("test.delete.access.checker")
public class TestDeleteAccessChecker implements AccessChecker {
    @Override
    public boolean checkAccess(Id objectId) {
        return true;
    }
}
