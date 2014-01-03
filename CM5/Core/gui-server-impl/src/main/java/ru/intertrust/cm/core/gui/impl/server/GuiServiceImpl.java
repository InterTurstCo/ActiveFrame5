package ru.intertrust.cm.core.gui.impl.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.impl.server.form.FormRetriever;
import ru.intertrust.cm.core.gui.impl.server.form.FormSaver;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.FormState;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.interceptor.Interceptors;
import java.lang.reflect.InvocationTargetException;

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
        String navigationPanelName = "panel";
        NavigationConfig navigationConfig = configurationExplorer.getConfig(NavigationConfig.class, navigationPanelName);
        return navigationConfig;
    }

    @Override
    public Dto executeCommand(Command command) throws GuiException {
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
    public FormDisplayData getForm(String domainObjectType) {
        FormRetriever formRetriever = (FormRetriever)
                applicationContext.getBean("formRetriever", sessionContext.getCallerPrincipal().getName());
        return formRetriever.getForm(domainObjectType);
    }

    public FormDisplayData getForm(Id domainObjectId) {
        FormRetriever formRetriever = (FormRetriever)
                applicationContext.getBean("formRetriever", sessionContext.getCallerPrincipal().getName());
        return formRetriever.getForm(domainObjectId);
    }

    public DomainObject saveForm(FormState formState) {
        FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver", formState);
        return formSaver.saveForm();
    }

    public SessionContext getSessionContext(){
            return sessionContext;
    }

}
