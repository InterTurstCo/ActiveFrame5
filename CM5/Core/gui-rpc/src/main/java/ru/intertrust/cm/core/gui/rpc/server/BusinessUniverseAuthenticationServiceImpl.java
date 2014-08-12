package ru.intertrust.cm.core.gui.rpc.server;

import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.LoginService;
import ru.intertrust.cm.core.gui.impl.server.LoginServiceImpl;
import ru.intertrust.cm.core.gui.model.LoginWindowInitialization;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseAuthenticationService;
import ru.intertrust.cm.core.model.AuthenticationException;

import javax.ejb.EJB;
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
    @EJB
    private GuiService guiService;

    @EJB
    private ConfigurationService configurationService;

    @Override
    public void login(UserCredentials userCredentials) throws AuthenticationException {
        LoginService guiService = new ru.intertrust.cm.core.gui.impl.server.LoginServiceImpl(); // todo - get rid
        guiService.login(getThreadLocalRequest(), userCredentials);
    }

    public void logout() {
        LoginService guiService = new LoginServiceImpl();
        guiService.logout(getThreadLocalRequest());
    }

    @Override
    public LoginWindowInitialization getLoginWindowInitialization(){
        LoginWindowInitialization initialization = new LoginWindowInitialization();
        initialization.setVersion(guiService.getCoreVersion());
        BusinessUniverseConfig businessUniverseConfig = configurationService.getConfig(BusinessUniverseConfig.class, BusinessUniverseConfig.NAME);
        initialization.setLoginScreenConfig(businessUniverseConfig.getLoginScreenConfig());
        GlobalSettingsConfig globalSettingsConfig = configurationService.getGlobalSettings();
        initialization.setGlobalProductTitle(globalSettingsConfig.getProductTitle());
        initialization.setGlobalProductVersion(globalSettingsConfig.getProductVersion());
        initialization.setProductVersion(guiService.getProductVersion(globalSettingsConfig.getProductVersion().getArchive()));
        return initialization;
    };
}
