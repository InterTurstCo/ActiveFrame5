package ru.intertrust.cm.core.gui.impl.server.action.access;

import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;

/**
 * Created by andrey on 19.12.14.
 */
public interface AccessChecker extends ComponentHandler {
    boolean checkAccess(Id objectId);
}
