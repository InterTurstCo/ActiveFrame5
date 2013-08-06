package ru.intertrust.cm.core.gui.rpc.api;

import com.google.gwt.user.client.rpc.RemoteService;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;

/**
 * @author Denis Mitavskiy
 *         Date: 31.07.13
 *         Time: 16:57
 */
public interface BusinessUniverseService extends RemoteService {
    BusinessUniverseInitialization getBusinessUniverseInitialization();
}
