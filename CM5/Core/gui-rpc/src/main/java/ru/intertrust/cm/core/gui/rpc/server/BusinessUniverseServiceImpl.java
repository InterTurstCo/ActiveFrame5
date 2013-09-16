package ru.intertrust.cm.core.gui.rpc.server;

import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.model.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.model.BusinessUniverseInitialization;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.form.Form;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseService;

import javax.ejb.EJB;
import javax.servlet.annotation.WebServlet;

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
    public Dto executeCommand(Command command) {
        return guiService.executeCommand(command);
    }

    @Override
    public Form getForm(Id domainObjectId) {
        return guiService.getForm(domainObjectId);
    }
}
