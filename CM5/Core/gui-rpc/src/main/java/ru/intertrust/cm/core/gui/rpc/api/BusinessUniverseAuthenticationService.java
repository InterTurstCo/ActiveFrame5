package ru.intertrust.cm.core.gui.rpc.api;

import com.google.gwt.user.client.rpc.RemoteService;
import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.gui.model.LoginWindowInitialization;
import ru.intertrust.cm.core.model.AuthenticationException;

/**
 * @author Denis Mitavskiy
 *         Date: 06.08.13
 *         Time: 17:44
 */
public interface BusinessUniverseAuthenticationService extends RemoteService {
    void login(UserCredentials userCredentials) throws AuthenticationException;
    void logout();
    LoginWindowInitialization getLoginWindowInitialization();
}
