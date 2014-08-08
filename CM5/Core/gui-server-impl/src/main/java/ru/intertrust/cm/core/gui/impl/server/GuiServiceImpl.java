package ru.intertrust.cm.core.gui.impl.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.impl.server.form.FormResolver;
import ru.intertrust.cm.core.gui.impl.server.form.FormRetriever;
import ru.intertrust.cm.core.gui.impl.server.form.FormSaver;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.NavigationTreeResolver;
import ru.intertrust.cm.core.gui.impl.server.util.VersionUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.FormState;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;

/**
 * Базовая реализация сервиса GUI
 * Author: Denis Mitavskiy
 * Date: 14.06.13
 * Time: 16:14
 */
@Stateless
@Local(GuiService.class)
@Remote(GuiService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class GuiServiceImpl extends AbstractGuiServiceImpl implements GuiService, GuiService.Remote {

    private static Logger log = LoggerFactory.getLogger(GuiServiceImpl.class);

    @Override
    public NavigationConfig getNavigationConfiguration() {
        NavigationTreeResolver navigationTreeResolver = (NavigationTreeResolver)
                                                                  applicationContext.getBean("navigationTreeResolver");
        return navigationTreeResolver.getNavigationPanel(sessionContext.getCallerPrincipal().getName());
/*
        String navigationPanelName = "panel";
        NavigationConfig navigationConfig = configurationExplorer.getConfig(NavigationConfig.class, navigationPanelName);
        return navigationConfig;
*/

    }

    @Override
    public Dto executeCommand(final Command command, final UserInfo userInfo) throws GuiException {
        final GuiContext guiCtx = GuiContext.get();
        guiCtx.setUserInfo(userInfo);
        ComponentHandler componentHandler = obtainHandler(command.getComponentName());
        if (componentHandler == null) {
            log.warn("handler for component '{}' not found", command.getComponentName());
            return null;
        }
        try {
            final Dto dto = (Dto) componentHandler.getClass().getMethod(command.getName(), Dto.class)
                    .invoke(componentHandler, command.getParameter());
            return dto;
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            throw new GuiException("No command + " + command.getName() + " implemented");
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof GuiException) {
                throw (GuiException) e.getCause();
            } else {
                log.error(e.getMessage(), e);
                throw new GuiException("Command can't be executed: " + command.getName());
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new GuiException("Command can't be executed: " + command.getName());
        }
    }

    @Override
    public FormDisplayData getForm(final String domainObjectType, final UserInfo userInfo) {
        FormRetriever formRetriever = getFormRetriever(userInfo);
        return formRetriever.getForm(domainObjectType);
    }

    @Override
    public FormDisplayData getForm(String domainObjectType, String domainObjectUpdaterName, Dto updaterContext, UserInfo userInfo) {
        FormRetriever formRetriever = getFormRetriever(userInfo);
        return formRetriever.getForm(domainObjectType, domainObjectUpdaterName, updaterContext);

    }

    @Override
    public FormDisplayData getForm(final Id domainObjectId, final UserInfo userInfo) {
        FormRetriever formRetriever = getFormRetriever(userInfo);
        return formRetriever.getForm(domainObjectId);
    }

    @Override
    public FormDisplayData getForm(Id domainObjectId, String domainObjectUpdaterName, Dto updaterContext, UserInfo userInfo) {
        FormRetriever formRetriever = getFormRetriever(userInfo);
        return formRetriever.getForm(domainObjectId, domainObjectUpdaterName, updaterContext);
    }

    @Override
    public FormDisplayData getSearchForm(String domainObjectType, HashSet<String> formFields, final UserInfo userInfo) {
        FormRetriever formRetriever = getFormRetriever(userInfo);
        return formRetriever.getSearchForm(domainObjectType, formFields);
    }

    @Override
    public FormDisplayData getReportForm(String reportName, String formName, UserInfo userInfo) {
        FormRetriever formRetriever = getFormRetriever(userInfo);
        return formRetriever.getReportForm(reportName, formName);
    }

    @Override
    public DomainObject saveForm(final FormState formState, final UserInfo userInfo) {
        final GuiContext guiCtx = GuiContext.get();
        guiCtx.setUserInfo(userInfo);
        FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver");
        formSaver.setContext(formState, null);
        return formSaver.saveForm();
    }

    @Override
    public String getUserUid() {
            return sessionContext.getCallerPrincipal().getName();
    }

    @Override
    public FormConfig getFormConfig(String typeName, String formType) {
        FormResolver formResolver = (FormResolver) applicationContext.getBean("formResolver");
        return formResolver.findFormConfig(typeName, formType, getUserUid());
    }

    private FormRetriever getFormRetriever(UserInfo userInfo) {
        final GuiContext guiCtx = GuiContext.get();
        guiCtx.setUserInfo(userInfo);
        return (FormRetriever) applicationContext.getBean("formRetriever");
    }

    public String getApplicationVersion() {
              VersionUtil version =  (VersionUtil) applicationContext.getBean("applicationVersion");
        return version.getApplicationVersion();
    }


}
