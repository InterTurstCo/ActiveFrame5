package ru.intertrust.cm.core.gui.rpc.server;

import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.gui.api.server.LoginService;
import ru.intertrust.cm.core.gui.impl.server.LoginServiceImpl;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseAuthenticationService;

import javax.servlet.annotation.WebServlet;

/**
 * @author Denis Mitavskiy
 *         Date: 06.08.13
 *         Time: 17:46
 */
@WebServlet(name = "BusinessUniverseAuthenticationService",
        urlPatterns = "/remote/BusinessUniverseAuthenticationService")
public class BusinessUniverseAuthenticationServiceImpl extends BaseService
        implements BusinessUniverseAuthenticationService {
    @Override
    public void login(UserCredentials userCredentials) {
        LoginService guiService = new ru.intertrust.cm.core.gui.impl.server.LoginServiceImpl(); // todo - get rid
        guiService.login(getThreadLocalRequest(), userCredentials);
    }

    public void logout() {
        LoginService guiService = new LoginServiceImpl();
        guiService.logout(getThreadLocalRequest());
    }
}
