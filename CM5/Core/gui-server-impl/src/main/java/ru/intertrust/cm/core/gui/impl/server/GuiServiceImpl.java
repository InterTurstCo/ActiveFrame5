package ru.intertrust.cm.core.gui.impl.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.config.gui.ValidatorConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.impl.server.form.FormResolver;
import ru.intertrust.cm.core.gui.impl.server.form.FormRetriever;
import ru.intertrust.cm.core.gui.impl.server.form.FormSaver;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.NavigationTreeResolver;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.impl.server.util.VersionUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.validation.ValidationException;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.List;

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
            throw new GuiException("Команда " + command.getName() + " не найдена");
        } catch (InvocationTargetException e) {
//            if (e.getCause() instanceof ValidationException) {
//                log.error(e.getTargetException().getMessage(), e.getTargetException());
//                throw (ValidationException)e.getTargetException();
//            }
            log.error("Ошибка вызова команды: " + e.getMessage(), e);
            throw new GuiException(e.getTargetException());
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new GuiException("Команда не может быть выполнена: " + command.getName(), e);
        }
    }

    @Override
    public FormDisplayData getForm(final String domainObjectType, final UserInfo userInfo, FormViewerConfig formViewerConfig) {
        FormRetriever formRetriever = getFormRetriever(userInfo);
        return formRetriever.getForm(domainObjectType, formViewerConfig);
    }

    @Override
    public FormDisplayData getForm(String domainObjectType, String domainObjectUpdaterName, Dto updaterContext, UserInfo userInfo,
                                   FormViewerConfig formViewerConfig) {
        FormRetriever formRetriever = getFormRetriever(userInfo);
        return formRetriever.getForm(domainObjectType, domainObjectUpdaterName, updaterContext, formViewerConfig);

    }

    @Override
    public FormDisplayData getForm(final Id domainObjectId, final UserInfo userInfo, FormViewerConfig formViewerConfig) {
        FormRetriever formRetriever = getFormRetriever(userInfo);
        return formRetriever.getForm(domainObjectId, formViewerConfig);
    }

    @Override
    public FormDisplayData getForm(Id domainObjectId, String domainObjectUpdaterName, Dto updaterContext, UserInfo userInfo,
                                   FormViewerConfig formViewerConfig) {
        FormRetriever formRetriever = getFormRetriever(userInfo);
        return formRetriever.getForm(domainObjectId, domainObjectUpdaterName, updaterContext, formViewerConfig);
    }

    @Override
    public FormDisplayData getForm(UserInfo userInfo, FormPluginConfig formPluginConfig) {
        FormRetriever formRetriever = getFormRetriever(userInfo);
        return formRetriever.getForm(formPluginConfig);
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
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public DomainObject saveForm(final FormState formState, final UserInfo userInfo, List<ValidatorConfig>
            validatorConfigs) {
        List<String> errorMessages = PluginHandlerHelper.doCustomServerSideValidation(formState, validatorConfigs);
        if (!errorMessages.isEmpty()) {
            throw new ValidationException("Server-side validation failed", errorMessages);
        }
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

    public String getCoreVersion() {
        VersionUtil version = (VersionUtil) applicationContext.getBean("applicationVersion");
        return version.getApplicationVersion();
    }

    public String getProductVersion(String jarName) {
        VersionUtil version = (VersionUtil) applicationContext.getBean("applicationVersion");
        return version.getProductVersion(jarName);
    }
}
