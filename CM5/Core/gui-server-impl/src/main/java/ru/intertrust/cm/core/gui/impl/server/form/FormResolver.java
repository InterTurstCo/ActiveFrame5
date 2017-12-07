package ru.intertrust.cm.core.gui.impl.server.form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.dto.Case;
import ru.intertrust.cm.core.business.api.dto.CaseInsensitiveHashMap;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.event.ConfigurationUpdateEvent;
import ru.intertrust.cm.core.config.gui.GroupConfig;
import ru.intertrust.cm.core.config.gui.GroupsConfig;
import ru.intertrust.cm.core.config.gui.UserConfig;
import ru.intertrust.cm.core.config.gui.UsersConfig;
import ru.intertrust.cm.core.config.gui.form.*;
import ru.intertrust.cm.core.config.localization.LocalizationKeys;
import ru.intertrust.cm.core.config.localization.MessageResourceProvider;
import ru.intertrust.cm.core.gui.api.server.GuiContext;
import ru.intertrust.cm.core.gui.api.server.plugin.FormMappingHandler;
import ru.intertrust.cm.core.gui.model.GuiException;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 21.10.13
 *         Time: 19:49
 */
public class FormResolver implements ApplicationListener<ConfigurationUpdateEvent>, FormMappingHandler {
    private static Logger log = LoggerFactory.getLogger(FormResolver.class);

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private PersonManagementService personManagementService;

    private FormsCache editingFormsCache;
    private FormsCache searchFormsCache;
    private FormsCache reportFormsCache;

    public FormResolver() {
    }

    @Override
    public void onApplicationEvent(ConfigurationUpdateEvent event) {
        if (event.configTypeChanged(FormConfig.class)) {
            resetCaches();
        }
    }

    public FormConfig findEditingFormConfig(DomainObject root, String userUid) {
        return findFormConfig(root.getTypeName(), FormConfig.TYPE_EDIT, userUid);
    }

    public FormConfig findSearchFormConfig(String domainObjectType, String userUid) {
        return findFormConfig(domainObjectType, FormConfig.TYPE_SEARCH, userUid);
    }

    public FormConfig findReportFormConfig(String reportName, String userUid) {
        return findFormConfig(reportName, FormConfig.TYPE_REPORT, userUid);
    }

    public FormConfig findFormConfig(String targetTypeName, String formType, String userUid) {
        // находится форма для данного контекста, учитывая факт того, переопределена ли форма для пользователя/роли,
        // если флаг "использовать по умолчанию" не установлен
        // в конечном итоге получаем FormConfig
        final FormsCache cache = findCache(formType);

        List<String> userFormConfigs = cache.getUserFormConfigs(userUid, targetTypeName);
        if (userFormConfigs != null && userFormConfigs.size() != 0) {
            if (userFormConfigs.size() > 1) {
                log.warn("There's " + userFormConfigs.size()
                        + " forms defined for Domain Object Type: " + targetTypeName + " and User: " + userUid);
            }
            String formName = userFormConfigs.get(0);
            return getLocalizedFormConfig(formName);
        }

        // todo define strategy of finding a form by role. which role? context role? or may be a static group?
        List<DomainObject> userGroups = personManagementService.getPersonGroups(personManagementService.getPersonId(userUid));
        Pair<String, Integer> maxPriorityPair = new Pair(null, Integer.MIN_VALUE);
        for (DomainObject userGroup : userGroups) {
            List<Pair<String, Integer>> groupFormConfigs = cache.getRoleFormConfigs(userGroup.getString("group_name"), targetTypeName);
            if (groupFormConfigs != null && groupFormConfigs.size() != 0) {
                for (Pair<String, Integer> pair : groupFormConfigs) {
                    Integer priority = pair.getSecond();
                    if (priority > maxPriorityPair.getSecond()) {
                        maxPriorityPair = pair;
                    }
                }
            }
        }
        if (maxPriorityPair.getFirst() != null) {
            String formName = maxPriorityPair.getFirst();
            return getLocalizedFormConfig(formName);
        }
        List<String> allFormConfigs = cache.getAllFormConfigs(targetTypeName);
        if (allFormConfigs == null || allFormConfigs.size() == 0) {
            log.warn("There's no default form defined for Domain Object Type: " + targetTypeName);
            return null;
        }

        String firstMetFormName = allFormConfigs.get(0);
        FormConfig firstMetForm = getLocalizedFormConfig(firstMetFormName);
        if (allFormConfigs.size() == 1) {
            return firstMetForm;
        }

        FormConfig defaultFormConfig = cache.getDefaultFormConfig(targetTypeName);
        if (defaultFormConfig != null) {
            return defaultFormConfig;
        }

        log.warn("There's no default form defined for Domain Object Type: " + targetTypeName);
        return firstMetForm;
    }

    public Set<String> findWidgetsToHide(String userUid, String form, String formType) {
        Set<String> widgetsToHide = new HashSet<>();

        final FormsCache cache = findCache(formType);

        List<String> widgetsToHideForUser = cache.widgetsByFormAndUser.get(new Pair(form, userUid));
        if (widgetsToHideForUser != null) {
            widgetsToHide.addAll(widgetsToHideForUser);
        }
        // find all groups of the user
        List<DomainObject> userGroups = personManagementService.getPersonGroups(personManagementService.getPersonId(userUid));
        for (DomainObject userGroup : userGroups) {
            String groupName = userGroup.getString("group_name");
            List<String> widgetsToHideForGroup = cache.widgetsByFormAndGroup.get(new Pair(form, groupName));
            if (widgetsToHideForGroup != null) {
                widgetsToHide.addAll(widgetsToHideForGroup);
            }
        }

        return widgetsToHide;
    }

    private FormsCache findCache(String formType) {
        if (editingFormsCache == null || searchFormsCache == null || reportFormsCache == null) {
            initCaches();
        }
        switch (formType) {
            case FormConfig.TYPE_EDIT:
                return editingFormsCache;
            case FormConfig.TYPE_SEARCH:
                return searchFormsCache;
            case FormConfig.TYPE_REPORT:
                return reportFormsCache;
        }
        return editingFormsCache;
    }

    @PostConstruct
    private synchronized void initCaches() {
        if (editingFormsCache == null) {
            editingFormsCache = new FormsCache(FormConfig.TYPE_EDIT);
        }
        if (searchFormsCache == null) {
            searchFormsCache = new FormsCache(FormConfig.TYPE_SEARCH);
        }
        if (reportFormsCache == null) {
            reportFormsCache = new FormsCache(FormConfig.TYPE_REPORT);
        }
    }

    private synchronized void resetCaches() {
        editingFormsCache = null;
        searchFormsCache = null;
        reportFormsCache = null;
    }

    private FormConfig getLocalizedFormConfig(String formName) {
        return configurationExplorer.getLocalizedPlainFormConfig(formName, GuiContext.getUserLocale());
    }

    private class FormsCache {
        private CaseInsensitiveHashMap<String> defaultFormByDomainObjectType; // <DO type, config name>
        private CaseInsensitiveHashMap<List<String>> allFormsByDomainObjectType; //<DO type, List<config name>>
        private HashMap<Pair<String, String>, List<Pair<String, Integer>>> formsByRoleAndDomainObjectType; //<<Role, DO type>, List<config name, priority>>
        private HashMap<Pair<String, String>, List<String>> formsByUserAndDomainObjectType; // <<User, DO type> , List<config name>>
        private Map<Pair<String, String>, List<String>> widgetsByFormAndUser;
        private Map<Pair<String, String>, List<String>> widgetsByFormAndGroup;

        private FormsCache(String formType) {

            defaultFormByDomainObjectType = new CaseInsensitiveHashMap<>();
            allFormsByDomainObjectType = new CaseInsensitiveHashMap<>();
            formsByRoleAndDomainObjectType = new HashMap<>();
            formsByUserAndDomainObjectType = new HashMap<>();
            widgetsByFormAndUser = new HashMap<>();
            widgetsByFormAndGroup = new HashMap<>();

            Map<Pair<String, String>, List<String>> widgetsByFormAndWidgetGroup = new HashMap<>();

            Collection <FormConfig> formConfigs = configurationExplorer.getConfigs(FormConfig.class);
            if (formConfigs == null) {
                formConfigs = Collections.EMPTY_LIST;
            }
            for (FormConfig formConfig : formConfigs) {
                final String currentFormType = formConfig.getType();
                if (currentFormType == null) {
                    log.warn("Form type is not defined for: " + formConfig.getName());
                    continue;
                }
                String domainObjectType = formConfig.getTargetTypeName(); // domain object type or report name
                if (domainObjectType == null) {
                    log.warn("Domain Object Type is not defined for form: " + formConfig.getName());
                    continue;
                }
                if (!formType.equals(currentFormType)) {
                    continue;
                }

                String domainObjectTypeInLowerCase = Case.toLower(domainObjectType);
                if (formConfig.isDefault()) {
                    if (defaultFormByDomainObjectType.containsKey(domainObjectType)) {
                        throw new GuiException(MessageResourceProvider.getMessage(LocalizationKeys.GUI_EXCEPTION_MANY_DEFAULT_FORMS,
                                GuiContext.getUserLocale(),
                                "There's more than 1 default form for type: "
                                ) + domainObjectType);
                    }
                    defaultFormByDomainObjectType.put(domainObjectTypeInLowerCase, formConfig.getName());
                }

                List<String> domainObjectTypeForms = allFormsByDomainObjectType.get(domainObjectType);
                if (domainObjectTypeForms == null) {
                    domainObjectTypeForms = new ArrayList<>();
                    allFormsByDomainObjectType.put(domainObjectTypeInLowerCase, domainObjectTypeForms);
                }
                domainObjectTypeForms.add(formConfig.getName());

                if (formConfig.getWidgetGroupsConfig() != null) {
                    for (WidgetGroupConfig widgetGroupConfig : formConfig.getWidgetGroupsConfig().getWidgetGroupConfigList()) {
                        String widgetGroupName = widgetGroupConfig.getName();
                        for (WidgetRefConfig widgetRefConfig : widgetGroupConfig.getWidgetRefConfigList()) {
                            List widgetsInGroup = widgetsByFormAndWidgetGroup.get(new Pair(formConfig.getName(), widgetGroupName));
                            if (widgetsInGroup == null) {
                                widgetsInGroup = new ArrayList<>();
                                widgetsByFormAndWidgetGroup.put(new Pair(formConfig.getName(), widgetGroupName), widgetsInGroup);
                            }
                            widgetsInGroup.add(widgetRefConfig.getId());
                        }
                    }
                }
            }

            Collection<FormMappingConfig> formMappingConfigs = getFormMappingConfigs(configurationExplorer);
            for (FormMappingConfig formMapping : formMappingConfigs) {
                String domainObjectType = formMapping.getDomainObjectType();
                FormConfig formConfig = configurationExplorer.getPlainFormConfig(formMapping.getForm());

                if (formConfig == null || !formType.equals(formConfig.getType())) {
                    continue;
                }
                fillRoleAndDomainObjectTypeFormMappings(formMapping, domainObjectType, formConfig);
                fillUserAndDomainObjectTypeFormMappings(formMapping, domainObjectType, formConfig);
            }

            Collection<FormWidgetAccessConfig> formWidgetAccessConfigs = getFormWidgetAccessConfigs
                    (configurationExplorer);
            for (FormWidgetAccessConfig formWidgetAccessConfig : formWidgetAccessConfigs) {
                String form = formWidgetAccessConfig.getForm();
                for (HideWidgetConfig hideWidgetConfig : formWidgetAccessConfig.getHideWidgetsConfigList()) {
                    List<String> widgets = new ArrayList<>();
                    if (hideWidgetConfig.getWidgetId() != null) {
                        widgets.add(hideWidgetConfig.getWidgetId());
                    } else if (hideWidgetConfig.getWidgetGroupId() != null) {
                        List<String> widgetsInGroup = widgetsByFormAndWidgetGroup.get(new Pair(form,
                                hideWidgetConfig.getWidgetGroupId()));
                        if (widgetsInGroup != null) {
                            widgets.addAll(widgetsInGroup);
                        }
                    }
                    for (String widget : widgets) {
                        fillWidgetsByFormAndUser(hideWidgetConfig, form, widget);
                        fillWidgetsByFormAndGroup(hideWidgetConfig, form, widget);
                    }
                }
            }
        }

        public FormConfig getDefaultFormConfig(String targetTypeName) {
            String formConfigName = defaultFormByDomainObjectType.get(targetTypeName);
            return getLocalizedFormConfig(formConfigName);
        }

        public List<String> getAllFormConfigs(String targetTypeName) {
            return allFormsByDomainObjectType.get(targetTypeName);
        }

        private List<Pair<String, Integer>> getRoleFormConfigs(String roleName, String domainObjectType) {
            return formsByRoleAndDomainObjectType.get(new Pair<>(roleName, domainObjectType));
        }

        public List<String> getUserFormConfigs(String userUid, String targetTypeName) {
            return formsByUserAndDomainObjectType.get(new Pair<>(userUid, targetTypeName));
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

        private Collection<FormWidgetAccessConfig> getFormWidgetAccessConfigs(ConfigurationExplorer explorer) {
            Collection<FormMappingsConfig> configs = explorer.getConfigs(FormMappingsConfig.class);
            if (configs == null || configs.isEmpty()) {
                return Collections.EMPTY_LIST;
            }
            ArrayList<FormWidgetAccessConfig> result = new ArrayList<>();
            for (FormMappingsConfig config : configs) {
                List<FormWidgetAccessConfig> widgetAccess = config.getFormWidgetAccessConfig();
                if (widgetAccess != null) {
                    result.addAll(widgetAccess);
                }
            }
            return result;
        }

        private void fillRoleAndDomainObjectTypeFormMappings(FormMappingConfig formMapping, String domainObjectType, FormConfig formConfig) {
            GroupsConfig groupsConfig = formMapping.getGroupsConfig();
            if (groupsConfig == null) {
                return;
            }
            List<GroupConfig> groupConfigs = groupsConfig.getGroupConfigList();
            if (groupConfigs == null || groupConfigs.size() == 0) {
                return;
            }
            for (GroupConfig groupConfig : groupConfigs) {
                String roleName = groupConfig.getName();
                Pair<String, String> roleAndDomainObjectType = new Pair<>(roleName, domainObjectType);
                List<Pair<String, Integer>> roleFormConfigs = formsByRoleAndDomainObjectType.get(roleAndDomainObjectType);
                if (roleFormConfigs == null) {
                    roleFormConfigs = new ArrayList<>();
                    formsByRoleAndDomainObjectType.put(roleAndDomainObjectType, roleFormConfigs);
                }
                roleFormConfigs.add(new Pair<>(formConfig.getName(), (groupConfig.getPriority() != null ? groupConfig.getPriority() : 0)));
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
                List<String> userFormConfigs = formsByUserAndDomainObjectType.get(userAndDomainObjectType);
                if (userFormConfigs == null) {
                    userFormConfigs = new ArrayList<>();
                    formsByUserAndDomainObjectType.put(userAndDomainObjectType, userFormConfigs);
                }
                userFormConfigs.add(formConfig.getName());
            }
        }

        private void fillWidgetsByFormAndUser(HideWidgetConfig hideWidgetConfig, String form, String widget) {
            UsersConfig usersConfig = hideWidgetConfig.getUsersConfig();
            if (usersConfig != null) {
                List<UserConfig> userConfigs = usersConfig.getUserConfigList();
                if (userConfigs == null || userConfigs.size() == 0) {
                    return;
                }
                for (UserConfig userConfig : userConfigs) {
                    String userUid = userConfig.getUid();
                    Pair<String, String> key = new Pair<>(form, userUid);
                    List<String> widgets = widgetsByFormAndUser.get(key);
                    if (widgets == null) {
                        widgets = new ArrayList<>();
                        widgetsByFormAndUser.put(key, widgets);
                    }
                    widgets.add(widget);
                }
            }
        }

        private void fillWidgetsByFormAndGroup(HideWidgetConfig hideWidgetConfig, String form, String widget) {
            GroupsConfig groupsConfig = hideWidgetConfig.getGroupsConfig();
            if (groupsConfig != null) {
                List<GroupConfig> groupConfigs = groupsConfig.getGroupConfigList();
                for (GroupConfig groupConfig : groupConfigs) {
                    String groupName = groupConfig.getName();
                    Pair<String, String> key = new Pair<>(form, groupName);
                    List<String> widgets = widgetsByFormAndGroup.get(key);
                    if (widgets == null) {
                        widgets = new ArrayList<>();
                        widgetsByFormAndGroup.put(key, widgets);
                    }
                    widgets.add(widget);
                }
            }
        }
    }
}
