package ru.intertrust.cm.core.gui.rpc.server;

import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.gui.api.server.LoginService;
import ru.intertrust.cm.core.gui.impl.server.LoginServiceImpl;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseService;

import javax.servlet.annotation.WebServlet;

/**
 * @author Denis Mitavskiy
 *         Date: 31.07.13
 *         Time: 13:57
 */
@WebServlet(name = "BusinessUniverseService", urlPatterns = "/ru.intertrust.cm.core.gui.impl.Login/BusinessUniverseService")
public class BusinessUniverseServiceImpl extends BaseService implements BusinessUniverseService {
    @Override
    public void login(UserCredentials userCredentials) {
        LoginService guiService = new LoginServiceImpl(); // todo - get rid
        guiService.login(getThreadLocalRequest(), userCredentials);
    }
}
