package ru.intertrust.cm.core.gui.rpc.server;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;

import ru.intertrust.cm.core.config.model.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.plugin.PluginData;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseService;

/**
 * @author Denis Mitavskiy
 *         Date: 31.07.13
 *         Time: 13:57
 */
@WebServlet(name = "BusinessUniverseService",
        urlPatterns = "/remote/BusinessUniverseService")
public class BusinessUniverseServiceImpl extends BaseService implements BusinessUniverseService {
    @EJB
    private GuiService guiService;

    @Override
    public BusinessUniverseInitialization getBusinessUniverseInitialization() {
        NavigationConfig navigationConfiguration = guiService.getNavigationConfiguration();
        return new BusinessUniverseInitialization();
    }

    @Override
    public PluginData executeCommand(Command command) {
        return guiService.executeCommand(command);
    }
}
