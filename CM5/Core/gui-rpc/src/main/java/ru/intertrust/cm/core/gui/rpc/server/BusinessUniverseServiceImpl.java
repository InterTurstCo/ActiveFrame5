package ru.intertrust.cm.core.gui.rpc.server;

import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.impl.server.GuiServiceImpl;
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

        GuiService guiService = new GuiServiceImpl(); // todo - get rid
        guiService.login(userCredentials);
    }
}
