package ru.intertrust.cm.core.gui.impl.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import ru.intertrust.cm.core.business.api.CollectionsService;
import ru.intertrust.cm.core.business.api.CrudService;
import ru.intertrust.cm.core.business.api.dto.*;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.model.gui.RoleConfig;
import ru.intertrust.cm.core.config.model.gui.RolesConfig;
import ru.intertrust.cm.core.config.model.gui.UserConfig;
import ru.intertrust.cm.core.config.model.gui.UsersConfig;
import ru.intertrust.cm.core.config.model.gui.form.FormConfig;
import ru.intertrust.cm.core.config.model.gui.form.FormMappingConfig;
import ru.intertrust.cm.core.config.model.gui.form.FormMappingsConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.FieldPathConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.LabelConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfig;
import ru.intertrust.cm.core.config.model.gui.form.widget.WidgetConfigurationConfig;
import ru.intertrust.cm.core.config.model.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.gui.api.server.ComponentHandler;
import ru.intertrust.cm.core.gui.api.server.GuiService;
import ru.intertrust.cm.core.gui.api.server.widget.WidgetHandler;
import ru.intertrust.cm.core.gui.model.Command;
import ru.intertrust.cm.core.gui.model.GuiException;
import ru.intertrust.cm.core.gui.model.form.FieldPath;
import ru.intertrust.cm.core.gui.model.form.Form;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetData;

import javax.annotation.Resource;
import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.ejb.*;
import javax.interceptor.Interceptors;
import java.util.*;

/**
 * Базовая реализация сервиса GUI
 * Author: Denis Mitavskiy
 * Date: 14.06.13
 * Time: 16:14
 */
@Stateless
@DeclareRoles("cm_user")
@RolesAllowed("cm_user")
@Local(GuiService.class)
@Remote(GuiService.Remote.class)
@Interceptors(SpringBeanAutowiringInterceptor.class)
public class GuiServiceImpl implements GuiService, GuiService.Remote {

    static Logger log = LoggerFactory.getLogger(GuiServiceImpl.class);

    @Autowired
    ApplicationContext applicationContext;

    @Resource
    SessionContext sessionContext;

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @EJB
    private CrudService crudService;

    @EJB
    private CrudService.Remote crudServiceRemote;

    @EJB
    private CollectionsService collectionsService;

    @Override
    public NavigationConfig getNavigationConfiguration() {
        String navigationPanelName = "panel";
        NavigationConfig navigationConfig = configurationExplorer.getConfig(NavigationConfig.class, navigationPanelName);
        return navigationConfig;
    }

    @Override
    public Dto executeCommand(Command command) {
        ComponentHandler componentHandler = obtainHandler(command.getComponentName());
        if (componentHandler == null) {
            log.warn("handler for component '{}' not found", command.getComponentName());
            return null;
        }
        try {
            return (Dto) componentHandler.getClass().getMethod(command.getName(), Dto.class)
                    .invoke(componentHandler, command.getParameter());
        } catch (NoSuchMethodException e) {
            log.error(e.getMessage(), e);
            throw new GuiException("No command + " + command.getName() + " implemented");
        } catch (Throwable e) {
            e.printStackTrace();
            throw new GuiException("Command can't be executed: " + command.getName());
        }
    }

    @Override
    public Form getForm(String domainObjectType) {
        DomainObject root = crudService.createDomainObject(domainObjectType);
        // todo: separate empty form?
        return buildDomainObjectForm(root);
    }

    public Form getForm(Id domainObjectId) {
        DomainObject root = crudService.find(domainObjectId);
        if (root == null) {
            throw new GuiException("Object with id: " + domainObjectId.toStringRepresentation() + " doesn't exist");
        }
        return buildDomainObjectForm(root);
    }

    public DomainObject saveForm(Form form) {
        FormConfig formConfig = configurationExplorer.getConfig(FormConfig.class, form.getName());
        WidgetConfigurationConfig widgetConfigurationConfig = formConfig.getWidgetConfigurationConfig();
        List<WidgetConfig> widgetConfigs = widgetConfigurationConfig.getWidgetConfigList();
        FormObjects formObjects = form.getObjects();

        HashSet<FieldPath> objectsFieldPathsToSave = new HashSet<>();
        for (WidgetConfig widgetConfig : widgetConfigs) {
            WidgetData widgetData = form.getWidgetData(widgetConfig.getId());
            if (widgetData == null) { // ignore - such data shouldn't be saved
                continue;
            }
            Value newValue = widgetData.toValue();
            FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
            Value oldValue = formObjects.getObjectValue(fieldPath);
            if (!newValue.equals(oldValue)) {
                formObjects.setObjectValue(fieldPath, newValue);
                objectsFieldPathsToSave.add(fieldPath.createFieldPathWithoutLastElement());
            }
        }
        ArrayList<DomainObject> toSave = new ArrayList<>(objectsFieldPathsToSave.size());
        // todo sort field paths in such a way that linked objects are saved first?
        // root DO is save separately as we should return it's identifier in case it's created from scratch
        boolean saveRoot = false;
        for (FieldPath fieldPath : objectsFieldPathsToSave) {
            if (fieldPath.isRoot()) {
                saveRoot = true;
                continue;
            }
            toSave.add(formObjects.getObject(fieldPath));
        }
        crudService.save(toSave);
        DomainObject rootDomainObject = formObjects.getRootObject();
        if (saveRoot) {
            return crudService.save(rootDomainObject);
        } else {
            return rootDomainObject;
        }
    }

    private Form buildDomainObjectForm(DomainObject root) {
        HashMap<String, WidgetData> widgetDataMap = new HashMap<>();
        FormConfig formConfig = findFormConfig(root);
        WidgetConfigurationConfig widgetConfigurationConfig = formConfig.getWidgetConfigurationConfig();
        List<WidgetConfig> widgetConfigs = widgetConfigurationConfig.getWidgetConfigList();
        FormObjects formObjects = getFormObjects(root, widgetConfigs);
        for (WidgetConfig config : widgetConfigs) {
            WidgetHandler componentHandler = obtainHandler(config.getComponentName());
            WidgetContext widgetContext = new WidgetContext(config, formObjects);
            widgetDataMap.put(config.getId(), componentHandler.getInitialDisplayData(widgetContext));
        }
        Form form = new Form(formConfig.getName(), formConfig.getMarkup(), widgetDataMap, formObjects, formConfig.getDebug());
        return form;
    }

    private static FormMappingsCache formMappingsCache; // todo drop this ugly thing
    private FormConfig findFormConfig(DomainObject root) {
        // по Id ищется тип доменного объекта
        // далее находится форма для данного контекста, учитывая факт того, переопределена ли форма для пользователя/роли,
        // если флаг "использовать по умолчанию" не установлен
        // в конечном итоге получаем FormConfig

        if (formMappingsCache == null) {
            formMappingsCache = new FormMappingsCache(configurationExplorer);
        }
        String typeName = root.getTypeName();
        String userUid = sessionContext.getCallerPrincipal().getName();
        List<FormConfig> userFormConfigs = formMappingsCache.getUserFormConfigs(userUid, typeName);
        if (userFormConfigs != null && userFormConfigs.size() != 0) {
            if (userFormConfigs.size() > 1) {
                log.warn("There's " + userFormConfigs.size()
                        + " forms defined for Domain Object Type: " + typeName + " and User: " + userUid);
            }
            return userFormConfigs.get(0);
        }

        // todo define strategy of finding a form by role. which role? context role? or may be a static group?
        List<FormConfig> allFormConfigs = formMappingsCache.getAllFormConfigs(typeName);
        if (allFormConfigs == null || allFormConfigs.size() == 0) {
            throw new GuiException("There's no form defined for Domain Object Type: " + typeName);
        }

        FormConfig firstMetForm = allFormConfigs.get(0);
        if (allFormConfigs.size() == 1) {
            return firstMetForm;
        }

        FormConfig defaultFormConfig = formMappingsCache.getDefaultFormConfig(typeName);
        if (defaultFormConfig != null) {
            return defaultFormConfig;
        }

        log.warn("There's no default form defined for Domain Object Type: " + typeName);
        return firstMetForm;
    }

    private List<FieldPath> getFieldPaths(List<WidgetConfig> configs) {
        List<FieldPath> paths = new ArrayList<>(configs.size());
        for (WidgetConfig config : configs) {
            FieldPathConfig fieldPathConfig = config.getFieldPathConfig();
            if (fieldPathConfig == null || fieldPathConfig.getValue() == null) {
                if (!(config instanceof LabelConfig)) {
                    throw new GuiException("Widget, id: " + config.getId() + " is not configured with Field Path");
                } else {
                    continue;
                }
            }
            paths.add(new FieldPath(fieldPathConfig.getValue()));
        }
        return paths;
    }

    private FormObjects getFormObjects(DomainObject root, List<WidgetConfig> widgetConfigs) {
        // не уверен, нужен ли здесь будет Business Object, но наверно нужен в некотором урезанном виде - для оптимистических блокировок

        List<FieldPath> fieldPaths = getFieldPaths(widgetConfigs);

        FormObjects formObjects = new FormObjects();
        formObjects.setRootObject(root);
        for (FieldPath fieldPath : fieldPaths) {
            DomainObject currentRoot = root;
            for (Iterator<FieldPath> subPathIterator = fieldPath.subPathIterator(); subPathIterator.hasNext(); ) {
                FieldPath subPath = subPathIterator.next();
                if (!subPathIterator.hasNext()) { // значит текущий путь указывает на Value и будет получаться из Domain Object
                    break; // ничего не делаем, а раз следующего нет, выходим из цикла
                }
                if (formObjects.isObjectSet(subPath)) {
                    continue;
                }

                String linkField = subPath.getLastElement();
                if (linkField.contains("^")) { // it's a "back-link"
                    //todo
                } else {
                    Id linkedObjectId = currentRoot.getReference(linkField);
                    if (linkedObjectId != null) {
                        DomainObject linkedDo = crudService.find(linkedObjectId);
                        formObjects.setObject(subPath, linkedDo);
                        currentRoot = linkedDo;
                    } else {
                        // текущий root становится null, таким образом все последующие вызовы бессмыссленны
                        break;

                        // todo или создавать пустой Domain Object? если мы разрешаем сохранение "новых" связанных
                        // объектов, то нужна для этого инфраструктура
                        // сценарий: у страны есть столица, а на форме показано название столицы. когда столица не назначена,
                        // поле пусто. Когда его заполняет пользователь, то такую столицу надо создать...
                    }
                }
            }
        }

        return formObjects;
    }

    private <T extends ComponentHandler> T obtainHandler(String componentName) {
        boolean containsHandler = applicationContext.containsBean(componentName);
        return containsHandler ? (T) applicationContext.getBean(componentName) : null;
    }

    private static class FormMappingsCache {
        private HashMap<String, FormConfig> defaultFormByDomainObjectType = new HashMap<>();
        private HashMap<String, List<FormConfig>> allFormsByDomainObjectType = new HashMap<>();
        private HashMap<Pair<String, String>, List<FormConfig>> formsByRoleAndDomainObjectType = new HashMap<>();
        private HashMap<Pair<String, String>, List<FormConfig>> formsByUserAndDomainObjectType = new HashMap<>();

        public FormMappingsCache(ConfigurationExplorer configExplorer) {
            Collection<FormConfig> formConfigs = configExplorer.getConfigs(FormConfig.class);
            if (formConfigs == null) {
                formConfigs = Collections.EMPTY_LIST;
            }
            for (FormConfig formConfig : formConfigs) {
                String domainObjectType = formConfig.getDomainObjectType();
                if (formConfig.isDefault()) {
                    if (defaultFormByDomainObjectType.containsKey(domainObjectType)) {
                        throw new GuiException("There's more than 1 default form for type: " + domainObjectType);
                    }
                    defaultFormByDomainObjectType.put(domainObjectType, formConfig);
                }

                List<FormConfig> domainObjectTypeForms = allFormsByDomainObjectType.get(domainObjectType);
                if (domainObjectTypeForms == null) {
                    domainObjectTypeForms = new ArrayList<>();
                    allFormsByDomainObjectType.put(domainObjectType, domainObjectTypeForms);
                }
                domainObjectTypeForms.add(formConfig);
            }

            Collection<FormMappingConfig> formMappingConfigs = getFormMappingConfigs(configExplorer);
            for (FormMappingConfig formMapping : formMappingConfigs) {
                String domainObjectType = formMapping.getDomainObjectType();
                FormConfig formConfig = configExplorer.getConfig(FormConfig.class, formMapping.getForm());
                fillRoleAndDomainObjectTypeFormMappings(formMapping, domainObjectType, formConfig);
                fillUserAndDomainObjectTypeFormMappings(formMapping, domainObjectType, formConfig);
            }
        }

        public FormConfig getDefaultFormConfig(String domainObjectType) {
            return defaultFormByDomainObjectType.get(domainObjectType);
        }

        public List<FormConfig> getAllFormConfigs(String domainObjectType) {
            return allFormsByDomainObjectType.get(domainObjectType);
        }

        public List<FormConfig> getRoleFormConfigs(String roleName, String domainObjectType) {
            return formsByRoleAndDomainObjectType.get(new Pair<>(roleName, domainObjectType));
        }

        public List<FormConfig> getUserFormConfigs(String userUid, String domainObjectType) {
            return formsByUserAndDomainObjectType.get(new Pair<>(userUid, domainObjectType));
        }

        private Collection<FormMappingConfig> getFormMappingConfigs(ConfigurationExplorer explorer) {
            Collection<FormMappingsConfig> configs = explorer.getConfigs(FormMappingsConfig.class);
            if (configs == null || configs.isEmpty()) {
                return Collections.EMPTY_LIST;
            }
            ArrayList<FormMappingConfig> result = new ArrayList<>();
            for (FormMappingsConfig config : configs) {
                List<FormMappingConfig> formMappings = config.getFormMappingConfigList();
                if (formMappings != null) {
                    result.addAll(formMappings);
                }
            }
            return result;
        }

        private void fillRoleAndDomainObjectTypeFormMappings(FormMappingConfig formMapping, String domainObjectType, FormConfig formConfig) {
            RolesConfig rolesConfig = formMapping.getRolesConfig();
            if (rolesConfig == null) {
                return;
            }
            List<RoleConfig> roleConfigs = rolesConfig.getRoleConfigList();
            if (roleConfigs == null || roleConfigs.size() == 0) {
                return;
            }
            for (RoleConfig roleConfig : roleConfigs) {
                String roleName = roleConfig.getName();
                Pair<String, String> roleAndDomainObjectType = new Pair<>(roleName, domainObjectType);
                List<FormConfig> roleFormConfigs = formsByRoleAndDomainObjectType.get(roleAndDomainObjectType);
                if (roleFormConfigs == null) {
                    roleFormConfigs = new ArrayList<>();
                    formsByRoleAndDomainObjectType.put(roleAndDomainObjectType, roleFormConfigs);
                }
                roleFormConfigs.add(formConfig);
            }
        }

        private void fillUserAndDomainObjectTypeFormMappings(FormMappingConfig formMapping, String domainObjectType, FormConfig formConfig) {
            UsersConfig usersConfig = formMapping.getUsersConfig();
            if (usersConfig == null) {
                return;
            }
            List<UserConfig> userConfigs = usersConfig.getUserConfigList();
            if (userConfigs == null || userConfigs.size() == 0) {
                return;
            }
            for (UserConfig userConfig : userConfigs) {
                String userUid = userConfig.getUid();
                Pair<String, String> userAndDomainObjectType = new Pair<>(userUid, domainObjectType);
                List<FormConfig> userFormConfigs = formsByUserAndDomainObjectType.get(userAndDomainObjectType);
                if (userFormConfigs == null) {
                    userFormConfigs = new ArrayList<>();
                    formsByUserAndDomainObjectType.put(userAndDomainObjectType, userFormConfigs);
                }
                userFormConfigs.add(formConfig);
            }
        }
    }
}
