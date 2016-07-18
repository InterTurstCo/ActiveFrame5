package ru.intertrust.cm.core.gui.impl.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;
import ru.intertrust.cm.core.config.gui.ValidatorConfig;
import ru.intertrust.cm.core.config.gui.action.SimpleActionConfig;
import ru.intertrust.cm.core.config.gui.business.universe.UserExtraInfoConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.action.ActionHandler;
import ru.intertrust.cm.core.gui.api.server.businessuniverse.UserExtraInfoBuilder;
import ru.intertrust.cm.core.gui.impl.server.form.FormResolver;
import ru.intertrust.cm.core.gui.impl.server.form.FormRetriever;
import ru.intertrust.cm.core.gui.impl.server.form.FormSaver;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.FormPluginHandler;
import ru.intertrust.cm.core.gui.impl.server.plugin.handlers.NavigationTreeResolver;
import ru.intertrust.cm.core.gui.impl.server.util.PluginHandlerHelper;
import ru.intertrust.cm.core.gui.impl.server.util.VersionUtil;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.UserExtraInfo;
import ru.intertrust.cm.core.gui.model.action.SimpleActionContext;
import ru.intertrust.cm.core.gui.model.action.SimpleActionData;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.util.PlaceholderResolver;
import ru.intertrust.cm.core.gui.model.validation.ValidationException;

import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

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

    @Autowired
    private ProfileService profileService;

    @EJB
    private GuiService newTransactionGuiService;

    private static Logger log = LoggerFactory.getLogger(GuiServiceImpl.class);

    @Override
    public NavigationConfig getNavigationConfiguration() {
        NavigationTreeResolver navigationTreeResolver = (NavigationTreeResolver)
                applicationContext.getBean("navigationTreeResolver");
        return navigationTreeResolver.getNavigationPanel(sessionContext.getCallerPrincipal().getName());
    }

    /**
     * Получить конфигурацию панели навигации по имени приложения (значение аттрибута
     * application при обьявлении navigation). Если панели с таким именем не найдено
     * возвращаем панель по умолчанию.
     * @param applicationName
     * @return
     */
    @Override
    public NavigationConfig getNavigationConfiguration(String applicationName) {

        Collection<NavigationConfig> navigationConfigs = configurationExplorer.getConfigs(NavigationConfig.class);
        for (NavigationConfig config : navigationConfigs) {
            if (config.getApplication() != null && config.getApplication().toLowerCase().equals(applicationName.toLowerCase())) {
                return config;
            }
        }
        return getNavigationConfiguration();
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
            throw new GuiException(MessageResourceProvider.getMessage(LocalizationKeys.GUI_EXCEPTION_COMMAND_NOT_FOUND,
                    GuiContext.getUserLocale(),"Команда ${commandName} не найдена"));
        } catch (InvocationTargetException e) {
//            if (e.getCause() instanceof ValidationException) {
//                log.error(e.getTargetException().getMessage(), e.getTargetException());
//                throw (ValidationException)e.getTargetException();
//            }
            log.error(MessageResourceProvider.getMessage(LocalizationKeys.GUI_EXCEPTION_COMMAND_CALL,
                    GuiContext.getUserLocale(), "Ошибка вызова команды: ") + e.getMessage(), e);
            throw new GuiException((e.getTargetException()==null)?e.getMessage():e.getTargetException().getMessage());
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new GuiException(MessageResourceProvider.getMessage(LocalizationKeys.GUI_EXCEPTION_COMMAND_EXECUTION,
                    GuiContext.getUserLocale(), "Команда не может быть выполнена: ")
                    + command.getName(), e);
        }
    }

    @Override
    public FormDisplayData getForm(Id domainObjectId, String domainObjectUpdaterName, Dto updaterContext, UserInfo userInfo,
                                   FormViewerConfig formViewerConfig) {
        return getFormRetriever(userInfo).getForm(domainObjectId, domainObjectUpdaterName, updaterContext, formViewerConfig);
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
    public DomainObject saveForm(final FormState formState, final UserInfo userInfo, List<ValidatorConfig>
            validatorConfigs) {
        return saveFormImpl(formState, userInfo, validatorConfigs);
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public DomainObject saveFormInNewTransaction(FormState formState, UserInfo userInfo, List<ValidatorConfig> validatorConfigs) {
        return saveFormImpl(formState, userInfo, validatorConfigs);
    }

    private DomainObject saveFormImpl(FormState formState, UserInfo userInfo, List<ValidatorConfig> validatorConfigs) {
        List<String> errorMessages = PluginHandlerHelper.doCustomServerSideValidation(formState, validatorConfigs,
                userInfo.getLocale());
        if (!errorMessages.isEmpty()) {
            throw new ValidationException("Server-side validation failed", errorMessages);
        }
        final GuiContext guiCtx = GuiContext.get();
        guiCtx.setUserInfo(userInfo);
        FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver");
        formSaver.setContext(formState, null);
        return formSaver.saveForm();
    }

    public SimpleActionData executeSimpleAction(SimpleActionContext context) {
        SimpleActionData result;
        if (((SimpleActionConfig) context.getActionConfig()).reReadInSameTransaction()) {
            result = executeSimpleActionImpl(context);
        } else {
            result = newTransactionGuiService.executeSimpleActionInNewTransaction(context, GuiContext.get().getUserInfo());
        }

        final SimpleActionConfig config = context.getActionConfig();
        FormPluginHandler handler = (FormPluginHandler) applicationContext.getBean("form.plugin");
        FormPluginConfig formPluginConfig = new FormPluginConfig(context.getRootObjectId());
        formPluginConfig.setPluginState(context.getPluginState());
        formPluginConfig.setFormViewerConfig(context.getViewerConfig());
        final FormPluginData formPluginData = handler.initialize(formPluginConfig);
        result.setPluginData(formPluginData);
        if (config.getAfterConfig() != null) {
            result.setOnSuccessMessage(config.getAfterConfig().getMessageConfig() == null
                    ? null
                    : config.getAfterConfig().getMessageConfig().getText());
        }
        return result;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public SimpleActionData executeSimpleActionInNewTransaction(SimpleActionContext context, UserInfo userInfo) {
        final GuiContext guiCtx = GuiContext.get();
        guiCtx.setUserInfo(userInfo);
        return executeSimpleActionImpl(context);
    }

    private SimpleActionData executeSimpleActionImpl(SimpleActionContext context) {
        String locale = GuiContext.getUserLocale();
        final List<String> errorMessages =
                PluginHandlerHelper.doServerSideValidation(context.getMainFormState(), applicationContext, locale);
        if (context.getConfirmFormState() != null) {
            errorMessages.addAll(PluginHandlerHelper.doServerSideValidation(
                    context.getConfirmFormState(), applicationContext, locale));
        }
        if (!errorMessages.isEmpty()) {
            throw new ValidationException(buildMessage(LocalizationKeys.SERVER_VALIDATION_EXCEPTION,
                    "Server-side validation failed"), errorMessages);
        }
        final SimpleActionConfig config = context.getActionConfig();
        final boolean isSaveContext = config.getBeforeConfig() == null
                ? true
                : config.getBeforeConfig().isSaveContext();
        DomainObject mainDomainObject = null;
        if (isSaveContext) {
            mainDomainObject = saveSimpleActionContext(context, locale, errorMessages, config);
        }
        final SimpleActionData result;
        if (SimpleActionContext.COMPONENT_NAME.equals(config.getActionHandler())) {
            result = new SimpleActionData();
        } else {
            final ActionHandler delegate = (ActionHandler) applicationContext.getBean(config.getActionHandler());
            result = (SimpleActionData) delegate.executeAction(context); // after commit???
        }
        if (isSaveContext) {
            result.setContextSaved(true);
            result.setSavedMainObjectId(mainDomainObject.getId());
        }
        return result;
    }

    private DomainObject saveSimpleActionContext(SimpleActionContext context, String locale, List<String> errorMessages,
                                                 SimpleActionConfig config) {
        DomainObject mainDomainObject;
        final UserInfo userInfo = GuiContext.get().getUserInfo();
        final FormState mainFormState = context.getMainFormState();
        final FormState confirmFormState = context.getConfirmFormState();

        List<ValidatorConfig> validators = config.isImmediate() ? null : config.getCustomValidators();
        if (confirmFormState != null) {
            // Если confirmState существует, должен быть и referenceFieldPath
            final FieldPath path = FieldPath.createPaths(
                    config.getBeforeConfig().getLinkedDomainObjectConfig().getReferenceFieldPath())[0];
            if (path.isMultiBackReference()) {
                throw new GuiException(buildMessage(LocalizationKeys.GUI_EXCEPTION_REF_PATH_NOT_SUPPORTED,
                        "Reference ${path} not supported", new Pair("path", path)));
            }
            final FormSaver formSaver = (FormSaver) applicationContext.getBean("formSaver");
            final Map<FieldPath, Value> values = new HashMap<>();
            if (path.isOneToOneBackReference()) {
                mainDomainObject = saveFormImpl(mainFormState, userInfo, validators);
                values.put(FieldPath.createPaths(path.getLinkToParentName())[0],
                        new ReferenceValue(mainDomainObject.getId()));
                formSaver.setContext(confirmFormState, values);
                formSaver.saveForm();
            } else {
                DomainObject confirmDomainObject = saveFormImpl(confirmFormState, userInfo, null);
                values.put(path, new ReferenceValue(confirmDomainObject.getId()));
                PluginHandlerHelper.doCustomServerSideValidation(mainFormState, validators, locale);
                if (!errorMessages.isEmpty()) {
                    throw new ValidationException(buildMessage(LocalizationKeys.SERVER_VALIDATION_EXCEPTION,
                            "Server-side validation failed"), errorMessages);
                }
                formSaver.setContext(mainFormState, values);
                mainDomainObject = formSaver.saveForm();
            }
        } else {
            mainDomainObject = saveFormImpl(mainFormState, userInfo, validators);
        }
        return mainDomainObject;
    }

    private String buildMessage(String message, String defaultValue, Pair<String, String>... params) {
        Map<String, String> paramsMap = new HashMap<>();
        for (Pair<String, String> pair : params) {
            paramsMap.put(pair.getFirst(), pair.getSecond());
        }
        return PlaceholderResolver.substitute(buildMessage(message, defaultValue), paramsMap);
    }

    private String buildMessage(String message, String defaultValue) {
        return MessageResourceProvider.getMessage(message, GuiContext.getUserLocale(),defaultValue);
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
        return version.getApplicationVersion(profileService.getPersonLocale());
    }

    public String getProductVersion(String jarName) {
        VersionUtil version = (VersionUtil) applicationContext.getBean("applicationVersion");
        return version.getProductVersion(jarName, profileService.getPersonLocale());
    }

    public UserExtraInfo getUserExtraInfo() {
        final BusinessUniverseConfig businessUniverseConfig = configurationExplorer.getConfig(BusinessUniverseConfig.class, BusinessUniverseConfig.NAME);
        final UserExtraInfoConfig userExtraInfoConfig = businessUniverseConfig.getUserExtraInfoConfig();
        String component = userExtraInfoConfig == null ? null : userExtraInfoConfig.getComponent();
        UserExtraInfoBuilder userExtraInfoBuilder = null;
        if (component != null && !component.isEmpty()) {
            userExtraInfoBuilder = (UserExtraInfoBuilder) applicationContext.getBean(component);
        }
        if (userExtraInfoBuilder == null) {
            userExtraInfoBuilder = (UserExtraInfoBuilder) applicationContext.getBean("default.user.extra.info.builder");
        }
        return userExtraInfoBuilder.getUserExtraInfo();
    }
}
