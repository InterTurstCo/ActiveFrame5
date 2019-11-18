package ru.intertrust.cm.core.gui.impl.server;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.interceptor.Interceptors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import ru.intertrust.cm.core.UserInfo;
import ru.intertrust.cm.core.business.api.AuditService;
import ru.intertrust.cm.core.business.api.ProfileService;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.DomainObjectVersion;
import ru.intertrust.cm.core.business.api.dto.Dto;
import ru.intertrust.cm.core.business.api.dto.GenericDomainObject;
import ru.intertrust.cm.core.business.api.dto.Id;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.business.api.dto.ReferenceValue;
import ru.intertrust.cm.core.business.api.dto.Value;
import ru.intertrust.cm.core.config.BusinessUniverseConfig;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.ValidatorConfig;
import ru.intertrust.cm.core.config.gui.action.AbstractActionConfig;
import ru.intertrust.cm.core.config.gui.action.ActionRefConfig;
import ru.intertrust.cm.core.config.gui.action.SimpleActionConfig;
import ru.intertrust.cm.core.config.gui.action.ToolBarConfig;
import ru.intertrust.cm.core.config.gui.business.universe.UserExtraInfoConfig;
import ru.intertrust.cm.core.config.gui.form.BodyConfig;
import ru.intertrust.cm.core.config.gui.form.CellConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.HeaderConfig;
import ru.intertrust.cm.core.config.gui.form.MarkupConfig;
import ru.intertrust.cm.core.config.gui.form.RowConfig;
import ru.intertrust.cm.core.config.gui.form.SingleEntryGroupListConfig;
import ru.intertrust.cm.core.config.gui.form.TabConfig;
import ru.intertrust.cm.core.config.gui.form.TabGroupConfig;
import ru.intertrust.cm.core.config.gui.form.TableLayoutConfig;
import ru.intertrust.cm.core.config.gui.form.widget.WidgetDisplayConfig;
import ru.intertrust.cm.core.config.gui.navigation.FormViewerConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.dao.api.DomainObjectTypeIdCache;
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
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.SingleObjectNode;
import ru.intertrust.cm.core.gui.model.form.widget.LabelState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginConfig;
import ru.intertrust.cm.core.gui.model.plugin.FormPluginData;
import ru.intertrust.cm.core.gui.model.util.PlaceholderResolver;
import ru.intertrust.cm.core.gui.model.validation.ValidationException;
import ru.intertrust.cm.core.util.SpringBeanAutowiringInterceptor;

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
    
    @Autowired
    private DomainObjectTypeIdCache domainObjectTypeIdCache;

    @Autowired
    private AuditService auditService;

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
            if (config.getApplication() != null && Case.toLower(config.getApplication()).equals(Case.toLower(applicationName))) {
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
        String typeName = domainObjectTypeIdCache.getName(domainObjectId);
        
        if (configurationExplorer.isAuditLogType(typeName)) {
            return getAuditForm(domainObjectId);
        }else {
            return getFormRetriever(userInfo).getForm(domainObjectId, domainObjectUpdaterName, updaterContext, formViewerConfig);
        }
    }

    private FormDisplayData getAuditForm(Id versionId) {
        String typeName = domainObjectTypeIdCache.getName(versionId);
        String auditedTypeName = typeName.substring(0, typeName.length() - 3);
        // Получение информации о версиях
        DomainObjectVersion domainObjectVersion = auditService.findVersion(versionId);
        DomainObjectVersion previousDomainObjectVersion = auditService.findPreviousVersion(versionId);
        FormDisplayData result = new FormDisplayData();
        ToolBarConfig toolBarConfig = new ToolBarConfig();
        toolBarConfig.setUseDefault(false);
        result.setToolBarConfig(toolBarConfig);
        List<AbstractActionConfig> actions = new ArrayList<AbstractActionConfig>();
        toolBarConfig.setActions(actions);
        
        // Кнопка закрыть
        ActionRefConfig closeAction = new ActionRefConfig();
        closeAction.setNameRef("aToggleEditOff");
        closeAction.setRendered("(toggle-edit and not preview) or (not toggle-edit and preview) or (toggle-edit and preview)");        
        closeAction.setMerged(false);
        closeAction.setOrder(1);
        actions.add(closeAction);

        // Данные, пустой доменный объект, иначе падаем
        FormObjects formObjects = new FormObjects();
        GenericDomainObject fakeDomainObject = new GenericDomainObject(typeName);
        fakeDomainObject.setId(versionId);
        formObjects.setRootNode(new SingleObjectNode(fakeDomainObject));
        
        List<String> showFields = getShowFields(domainObjectVersion.getFields());
        
        // Настройка виджетов
        Map<String, WidgetState> widgetStateMap = new HashMap<String, WidgetState>();
        widgetStateMap.put("type_name", new LabelState(auditedTypeName));
        
        // Маппинг типов виджетов
        Map<String, String> widgetComponents = new HashMap<String, String>();
        widgetComponents.put("type_name", "label");
        
        // Разметка body
        BodyConfig bodyConfig = new BodyConfig();
        TabConfig tabConfig = new TabConfig();
        tabConfig.setGroupList(new SingleEntryGroupListConfig());
        TabGroupConfig tabGroupConfig = new TabGroupConfig();
        tabGroupConfig.setLayout(new TableLayoutConfig());

        RowConfig headerRowConfig = new RowConfig();
        tabGroupConfig.getLayout().getRows().add(headerRowConfig);
        addFieldWidgets("header_name", "Атрибут", widgetStateMap, widgetComponents, headerRowConfig.getCells(), null, "underline");
        if (domainObjectVersion.getLong("operation") == 2 || domainObjectVersion.getLong("operation") == 3) {
            addFieldWidgets("header_old", "Старое значение", widgetStateMap, widgetComponents, headerRowConfig.getCells(), null, "underline");
        }
        if (domainObjectVersion.getLong("operation") == 2 || domainObjectVersion.getLong("operation") == 1) {
            addFieldWidgets("header_new", "Новое значение", widgetStateMap, widgetComponents, headerRowConfig.getCells(), null, "underline");
        }
        
        for (String field : showFields) {
            // Имя атрибута
            RowConfig fieldRowConfig = new RowConfig();
            tabGroupConfig.getLayout().getRows().add(fieldRowConfig);

            addFieldWidgets(field + "_label", field, widgetStateMap, widgetComponents, fieldRowConfig.getCells(), null, null);
            
            Set<String> changedFields = new HashSet<String>();
            // Изменение, и удаление формируем значения из предыдущей версии
            if (domainObjectVersion.getLong("operation") == 2 || domainObjectVersion.getLong("operation") == 3) {
                String value = "Данные удалены";
                if (previousDomainObjectVersion != null) {
                    value = previousDomainObjectVersion.getValue(field).toString();
                    if (domainObjectVersion.getLong("operation") == 2 && 
                            ((previousDomainObjectVersion.getValue(field) == null && domainObjectVersion.getValue(field) != null)
                            || (previousDomainObjectVersion.getValue(field) != null 
                                && !previousDomainObjectVersion.getValue(field).equals(domainObjectVersion.getValue(field))))) {
                        changedFields.add(field);
                    }
                }
                String textDecoration = null;
                String backgroundColor = null;
                if (changedFields.contains(field)) {
                    textDecoration = "line-through";
                    backgroundColor = "#FFBFAE";
                }
                
                addFieldWidgets(field + "_old", value, widgetStateMap, widgetComponents, fieldRowConfig.getCells(), backgroundColor, textDecoration);
            }
            
            // Создание и изменение, отображаем текущие данные
            if (domainObjectVersion.getLong("operation") == 1 || domainObjectVersion.getLong("operation") == 2) {
                String backgroundColor = null;
                if (changedFields.contains(field)) {
                    backgroundColor = "#DAF7A6";
                }
                addFieldWidgets(field + "_new", domainObjectVersion.getValue(field).toString(), widgetStateMap, widgetComponents, fieldRowConfig.getCells(), backgroundColor, null);
            }
        }
        
        tabConfig.getGroupList().getTabGroupConfigs().add(tabGroupConfig);
        bodyConfig.getTabs().add(tabConfig);
        
        // Разметка header
        HeaderConfig headerConfig = new HeaderConfig();
        headerConfig.setTableLayout(new TableLayoutConfig());
        
        // Имя типа
        RowConfig rowConfig = new RowConfig();
        headerConfig.getTableLayout().getRows().add(rowConfig);
        addFieldWidgets("type_name", "Тип: " + auditedTypeName, widgetStateMap, widgetComponents, rowConfig.getCells(), null, null);

        // Операция
        rowConfig = new RowConfig();
        headerConfig.getTableLayout().getRows().add(rowConfig);
        addFieldWidgets("operation", "Действие: " + getOperation(domainObjectVersion.getLong("operation")), widgetStateMap, widgetComponents, rowConfig.getCells(), null, null);
        
        // Идентификатор
        rowConfig = new RowConfig();
        headerConfig.getTableLayout().getRows().add(rowConfig);
        addFieldWidgets("id", "ID: " + domainObjectVersion.getReference("domain_object_id").toStringRepresentation(), 
                widgetStateMap, widgetComponents, rowConfig.getCells(), null, null);
        

        // Формирование результата
        FormState formState = new FormState("universal_audit_form", widgetStateMap, formObjects, widgetComponents, null, null);
        result.setFormState(formState);

        MarkupConfig markupConfig = new MarkupConfig();
        markupConfig.setBody(bodyConfig);
        markupConfig.setHeader(headerConfig);
        result.setMarkup(markupConfig);
        
        return result;
    }

    private String getOperation(Long operation) {
        if (operation == 1) {
            return "Создание";
        }else if (operation == 2) {
            return "Изменение";
        }else {
            return "Удаление";
        }
    }

    private void addFieldWidgets(String field, String value, Map<String, WidgetState> widgetStateMap, Map<String, String> widgetComponents, 
            List<CellConfig> cels, String backgroundColor, String textDecoration) {
        String oldValueWidgetId = field + "_old";
        
        LabelState label = new LabelState(value);
        label.setTextDecoration(textDecoration);
        label.setBackgroundColor(backgroundColor);
        
        widgetStateMap.put(oldValueWidgetId, label);         

        widgetComponents.put(oldValueWidgetId, "label");

        CellConfig cellConfig = new CellConfig();
        cellConfig.setWidgetDisplayConfig(new WidgetDisplayConfig());
        cellConfig.getWidgetDisplayConfig().setId(oldValueWidgetId);
        cels.add(cellConfig);                        
    }
    
    private List<String> getShowFields(ArrayList<String> fields) {
        List<String> result = new ArrayList<String>();
        for (String field : fields) {
            if (!field.equalsIgnoreCase("operation")
                    && !field.equalsIgnoreCase("domain_object_id")) {
                result.add(field);
            }
        }
        return result;
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
      if (!result.getDeleteAction()) {
        final FormPluginData formPluginData = handler.initialize(formPluginConfig);
        result.setPluginData(formPluginData);
        if (config.getAfterConfig() != null && result.getOnSuccessMessage()==null) {
          result.setOnSuccessMessage(config.getAfterConfig().getMessageConfig() == null
              ? null
              : config.getAfterConfig().getMessageConfig().getText());
        }
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
