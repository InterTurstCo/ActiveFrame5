package ru.intertrust.cm.core.gui.api.server.businessuniverse;

import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.model.UserExtraInfo;

/**
 * @author Denis Mitavskiy
 *         Date: 15.04.2015
 *         Time: 15:51
 */
public interface UserExtraInfoBuilder extends ComponentHandler {
    UserExtraInfo getUserExtraInfo();
}
