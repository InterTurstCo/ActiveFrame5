package ru.intertrust.cm.core.gui.rpc.server;

import java.util.Map;

import javax.ejb.EJB;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import ru.intertrust.cm.core.business.api.ConfigurationService;
import ru.intertrust.cm.core.business.api.dto.UserCredentials;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;
import ru.intertrust.cm.core.config.GlobalSettingsConfig;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.dao.api.ExtensionService;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.LoginService;
import ru.intertrust.cm.core.gui.api.server.extension.AuthenticationExtentionHandler;
import ru.intertrust.cm.core.gui.impl.server.LoginServiceImpl;
import ru.intertrust.cm.core.gui.model.LoginWindowInitialization;
import ru.intertrust.cm.core.gui.rpc.api.BusinessUniverseAuthenticationService;
import ru.intertrust.cm.core.model.AuthenticationException;

/**
 * @author Denis Mitavskiy
 *         Date: 06.08.13
 *         Time: 17:46
 */
@WebServlet(name = "BusinessUniverseAuthenticationService",
        urlPatterns = "/remote/BusinessUniverseAuthenticationService")
public class BusinessUniverseAuthenticationServiceImpl extends BaseService
        implements BusinessUniverseAuthenticationService {
    private static final long serialVersionUID = 8839303089066920446L;

    @EJB
    private GuiService guiService;

    @EJB
    private ConfigurationService configurationService;
    
    private ExtensionService extensionService;    
    
    @Override
    public void init(ServletConfig config) throws ServletException {
      super.init(config);     
      ApplicationContext ctx = WebApplicationContextUtils
              .getRequiredWebApplicationContext(config.getServletContext());
      this.extensionService = ctx.getBean(ExtensionService.class);      
    }    

    @Override
    public void login(UserCredentials userCredentials) throws AuthenticationException {
        LoginService loginService = new ru.intertrust.cm.core.gui.impl.server.LoginServiceImpl(); // todo - get rid
        loginService.login(getThreadLocalRequest(), userCredentials);
    }

    public void logout() {
        LoginService guiService = new LoginServiceImpl();
        guiService.logout(getThreadLocalRequest());
        
        //Вызов точки расширения после логаута, в точках расширения должны удалятся ранее сохраненные информация о пользователе
        AuthenticationExtentionHandler authExtHandler = extensionService.getExtentionPoint(AuthenticationExtentionHandler.class, null);
        authExtHandler.onAfterLogout(getThreadLocalRequest(), getThreadLocalResponse());        
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
        initialization.setProductVersion(globalSettingsConfig.getProductVersion() == null || globalSettingsConfig.getProductVersion().getArchive() == null ? null : guiService.getProductVersion(globalSettingsConfig.getProductVersion().getArchive()));
        if (globalSettingsConfig.getDefaultLocaleConfig() != null) {
            Map<String, String> messages = MessageResourceProvider.getMessages(globalSettingsConfig.getDefaultLocaleConfig().getName());
            initialization.setLocalizedResources(messages);
        }
        return initialization;
    }
}
