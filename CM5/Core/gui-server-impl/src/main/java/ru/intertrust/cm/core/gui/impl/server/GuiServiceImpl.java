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
import ru.intertrust.cm.core.config.model.FieldConfig;
import ru.intertrust.cm.core.config.model.ReferenceFieldConfig;
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
import ru.intertrust.cm.core.gui.model.form.FormDisplayData;
import ru.intertrust.cm.core.gui.model.form.FormObjects;
import ru.intertrust.cm.core.gui.model.form.FormState;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetContext;
import ru.intertrust.cm.core.gui.model.form.widget.WidgetState;

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

    private static Logger log = LoggerFactory.getLogger(GuiServiceImpl.class);

    @Autowired
    private ApplicationContext applicationContext;

    @Resource
    private SessionContext sessionContext;

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
    public FormDisplayData getForm(String domainObjectType) {
        DomainObject root = crudService.createDomainObject(domainObjectType);
        // todo: separate empty form?
        return buildDomainObjectForm(root);
    }

    public FormDisplayData getForm(Id domainObjectId) {
        DomainObject root = crudService.find(domainObjectId);
        if (root == null) {
            throw new GuiException("Object with id: " + domainObjectId.toStringRepresentation() + " doesn't exist");
        }
        return buildDomainObjectForm(root);
    }

    public DomainObject saveForm(FormState formState) {
        FormConfig formConfig = configurationExplorer.getConfig(FormConfig.class, formState.getName());
        WidgetConfigurationConfig widgetConfigurationConfig = formConfig.getWidgetConfigurationConfig();
        List<WidgetConfig> widgetConfigs = widgetConfigurationConfig.getWidgetConfigList();
        FormObjects formObjects = formState.getObjects();

        HashSet<FieldPath> objectsFieldPathsToSave = new HashSet<>();
        for (WidgetConfig widgetConfig : widgetConfigs) {
            WidgetState widgetState = formState.getWidgetState(widgetConfig.getId());
            if (widgetState == null) { // ignore - such data shouldn't be saved
                continue;
            }
            Value newValue = widgetState.toValue();
            FieldPath fieldPath = new FieldPath(widgetConfig.getFieldPathConfig().getValue());
            Value oldValue = formObjects.getObjectValue(fieldPath);
            if (!areValuesSemanticallyEqual(newValue, oldValue)) {
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

    private boolean areValuesSemanticallyEqual(Value newValue, Value oldValue) {
        boolean newValueEmpty = newValue == null || newValue.get() == null;
        boolean oldValueEmpty = oldValue == null || oldValue.get() == null;
        if (newValueEmpty && oldValueEmpty) {
            return true;
        }
        if (newValueEmpty || oldValueEmpty) { // something is NOT empty then (prev. condition)
            return false;
        }
        return newValue.equals(oldValue);
    }

    private FormDisplayData buildDomainObjectForm(DomainObject root) {
        FormConfig formConfig = findFormConfig(root);
        List<WidgetConfig> widgetConfigs = formConfig.getWidgetConfigurationConfig().getWidgetConfigList();
        HashMap<String, WidgetState> widgetStateMap = new HashMap<>(widgetConfigs.size());
        HashMap<String, String> widgetComponents = new HashMap<>(widgetConfigs.size());
        ProcessedWidgetsDetail processedWidgetsDetail = new ProcessedWidgetsDetail(widgetConfigs);
        FormObjects formObjects = getFormObjects(root, processedWidgetsDetail);
        for (WidgetConfig config : widgetConfigs) {
            String widgetId = config.getId();
            WidgetHandler componentHandler = obtainHandler(config.getComponentName());
            WidgetContext widgetContext = new WidgetContext(config, formObjects);
            WidgetState initialState = componentHandler.getInitialState(widgetContext);
            initialState.setEditable(processedWidgetsDetail.isWidgetEditable(config));
            widgetStateMap.put(widgetId, initialState);
            widgetComponents.put(widgetId, config.getComponentName());
        }
        FormState formState = new FormState(formConfig.getName(), widgetStateMap, formObjects);
        return new FormDisplayData(formState, formConfig.getMarkup(), widgetComponents, formConfig.getDebug(), true);
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

    private List<Pair<FieldPath, WidgetConfig>> findMatchingFieldPaths(List<WidgetConfig> configs) {
        List<Pair<FieldPath, WidgetConfig>> paths = new ArrayList<>(configs.size());
        for (WidgetConfig config : configs) {
            FieldPathConfig fieldPathConfig = config.getFieldPathConfig();
            if (fieldPathConfig == null || fieldPathConfig.getValue() == null) {
                if (!(config instanceof LabelConfig)) {
                    throw new GuiException("Widget, id: " + config.getId() + " is not configured with Field Path");
                } else {
                    continue;
                }
            }
            paths.add(new Pair<>(new FieldPath(fieldPathConfig.getValue()), config));
        }
        return paths;
    }

    private FormObjects getFormObjects(DomainObject root, ProcessedWidgetsDetail processedWidgetsDetail) {
        FormObjects formObjects = new FormObjects();
        formObjects.setRootObject(root);
        for (WidgetDetail widgetDetail : processedWidgetsDetail.getWidgetDetails()) {
            DomainObject currentRoot = root;
            for (Iterator<FieldPath> subPathIterator = widgetDetail.fieldPath.subPathIterator(); subPathIterator.hasNext(); ) {
                FieldPath subPath = subPathIterator.next();
                if (!subPathIterator.hasNext()) {
                    break; // current path is pointing to a Value and will be found in Domain Object, nothing to do else
                }

                // sub-path points to an object referenced from current root
                if (formObjects.isObjectSet(subPath)) {
                    continue;
                }

                String linkPath = subPath.getLastElement();
                DomainObject linkedDo = findLinkedDomainObject(currentRoot, linkPath);

                if (linkedDo != null) {
                    formObjects.setObject(subPath, linkedDo);
                    currentRoot = linkedDo;
                } else {
                    // current root becomes null, thus further iterations don't make sense
                    break;

                    // todo or create an empty Domain Object?
                    // if we allow "new" linked objects saving, appropriate infrastructure required
                    // scenario: country has a capital and form displays only capital's name. when capital isn't defined
                    // the field is empty. But when this field is filled by user, such capital (city) should be created..
                }
            }
        }

        return formObjects;
    }

    private DomainObject findLinkedDomainObject(DomainObject domainObject, String linkPath) {
        if (!linkPath.contains("^")) {
            Id linkedObjectId = domainObject.getReference(linkPath);
            return linkedObjectId == null ? null : crudService.find(linkedObjectId);
        }

        // it's a "back-link" (like country_best_friend^country)
        String[] domainObjectTypeAndReference = linkPath.split("\\^");
        if (domainObjectTypeAndReference.length != 2) {
            throw new GuiException("Invalid reference: " + linkPath);
        }
        String linkedDomainObjectType = domainObjectTypeAndReference[0];
        String referenceField = domainObjectTypeAndReference[1];
        FieldConfig fieldConfig = configurationExplorer.getFieldConfig(linkedDomainObjectType, referenceField);
        if (fieldConfig == null) {
            throw new GuiException(linkPath + " is referencing a non-existing type/field");
        }
        if (!(fieldConfig instanceof ReferenceFieldConfig)) {
            throw new GuiException(linkPath + " is not pointing to a reference");
        }
        ReferenceFieldConfig referenceFieldConfig = (ReferenceFieldConfig) fieldConfig;
        if (!referenceFieldConfig.getType().equals(domainObject.getTypeName())) {
            throw new GuiException(linkPath + " is not of type: " + domainObject.getTypeName());
        }

        // todo after CMFIVE-122 is done - get the first two linked objects, not everything!
        // todo after cardinality functionality is developed, check cardinality (static-check, not runtime)
        if (domainObject.getId() == null) {
            return null;
        }
        List<DomainObject> linkedDomainObjects =
                crudService.findLinkedDomainObjects(domainObject.getId(), linkedDomainObjectType, referenceField);
        if (linkedDomainObjects.size() > 1) {
            // such situation means that one field of a collection of DOs should be displayed/edited
            // for example names of country's cities. this is not supported, such behavior is handled by
            // widgets configured by back-link path
            throw new GuiException(linkPath + " is pointing to a collection of fields values");
        }

        return linkedDomainObjects.isEmpty() ? null : linkedDomainObjects.get(0);
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

    private static class ProcessedWidgetsDetail {
        private ArrayList<WidgetDetail> widgetDetails;
        private HashMap<String, FieldPath> fieldPathsByWidgetId;

        public ProcessedWidgetsDetail(List<WidgetConfig> configs) {
            widgetDetails = new ArrayList<>(configs.size());
            fieldPathsByWidgetId = new HashMap<>(configs.size());
            for (WidgetConfig config : configs) {
                FieldPathConfig fieldPathConfig = config.getFieldPathConfig();
                if (fieldPathConfig == null || fieldPathConfig.getValue() == null) {
                    if (!(config instanceof LabelConfig)) {
                        throw new GuiException("Widget, id: " + config.getId() + " is not configured with Field Path");
                    } else {
                        continue;
                    }
                }
                FieldPath fieldPath = new FieldPath(fieldPathConfig.getValue());
                widgetDetails.add(new WidgetDetail(config, fieldPath));
                fieldPathsByWidgetId.put(config.getId(), fieldPath);
            }
        }

        public ArrayList<WidgetDetail> getWidgetDetails() {
            return widgetDetails;
        }

        public boolean isWidgetEditable(WidgetConfig widgetConfig) {
            // it's editable only if widget is editable by nature and if it's a property of the very root object
            FieldPath fieldPath = fieldPathsByWidgetId.get(widgetConfig.getId());
            return fieldPath != null && fieldPath.size() == 1;
        }
    }

    private static class WidgetDetail {
        public final WidgetConfig widgetConfig;
        public final FieldPath fieldPath;

        private WidgetDetail(WidgetConfig widgetConfig, FieldPath fieldPath) {
            this.widgetConfig = widgetConfig;
            this.fieldPath = fieldPath;
        }
    }
}
