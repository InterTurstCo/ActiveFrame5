package ru.intertrust.cm.core.gui.rpc.api;

import com.google.gwt.user.client.rpc.RemoteService;
import ru.intertrust.cm.core.business.api.dto.UserCredentials;

/**
 * @author Denis Mitavskiy
 *         Date: 06.08.13
 *         Time: 17:44
 */
public interface BusinessUniverseAuthenticationService extends RemoteService {
    void login(UserCredentials userCredentials);
}
