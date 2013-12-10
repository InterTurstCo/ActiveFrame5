package ru.intertrust.cm.core.gui.impl.server.form;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.gui.RoleConfig;
import ru.intertrust.cm.core.config.gui.RolesConfig;
import ru.intertrust.cm.core.config.gui.UserConfig;
import ru.intertrust.cm.core.config.gui.UsersConfig;
import ru.intertrust.cm.core.config.gui.form.FormConfig;
import ru.intertrust.cm.core.config.gui.form.FormMappingConfig;
import ru.intertrust.cm.core.config.gui.form.FormMappingsConfig;
import ru.intertrust.cm.core.gui.model.GuiException;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * @author Denis Mitavskiy
 *         Date: 21.10.13
 *         Time: 19:49
 */
public class FormResolver {
    private static Logger log = LoggerFactory.getLogger(FormResolver.class);

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    private HashMap<String, FormConfig> defaultFormByDomainObjectType;
    private HashMap<String, List<FormConfig>> allFormsByDomainObjectType;
    private HashMap<Pair<String, String>, List<FormConfig>> formsByRoleAndDomainObjectType;
    private HashMap<Pair<String, String>, List<FormConfig>> formsByUserAndDomainObjectType;

    public FormResolver() {
    }

    public FormConfig findFormConfig(DomainObject root, String userUid) {
        // по Id ищется тип доменного объекта
        // далее находится форма для данного контекста, учитывая факт того, переопределена ли форма для пользователя/роли,
        // если флаг "использовать по умолчанию" не установлен
        // в конечном итоге получаем FormConfig

        String typeName = root.getTypeName().toLowerCase();
        List<FormConfig> userFormConfigs = getUserFormConfigs(userUid, typeName);
        if (userFormConfigs != null && userFormConfigs.size() != 0) {
            if (userFormConfigs.size() > 1) {
                log.warn("There's " + userFormConfigs.size()
                        + " forms defined for Domain Object Type: " + typeName + " and User: " + userUid);
            }
            return userFormConfigs.get(0);
        }

        // todo define strategy of finding a form by role. which role? context role? or may be a static group?
        List<FormConfig> allFormConfigs = getAllFormConfigs(typeName);
        if (allFormConfigs == null || allFormConfigs.size() == 0) {
            throw new GuiException("There's no form defined for Domain Object Type: " + typeName);
        }

        FormConfig firstMetForm = allFormConfigs.get(0);
        if (allFormConfigs.size() == 1) {
            return firstMetForm;
        }

        FormConfig defaultFormConfig = getDefaultFormConfig(typeName);
        if (defaultFormConfig != null) {
            return defaultFormConfig;
        }

        log.warn("There's no default form defined for Domain Object Type: " + typeName);
        return firstMetForm;
    }

    @PostConstruct
    private void initCaches() {
        defaultFormByDomainObjectType = new HashMap<>();
        allFormsByDomainObjectType = new HashMap<>();
        formsByRoleAndDomainObjectType = new HashMap<>();
        formsByUserAndDomainObjectType = new HashMap<>();

        Collection<FormConfig> formConfigs = configurationExplorer.getConfigs(FormConfig.class);
        if (formConfigs == null) {
            formConfigs = Collections.EMPTY_LIST;
        }
        for (FormConfig formConfig : formConfigs) {
            String domainObjectType = formConfig.getDomainObjectType();
            String domainObjectTypeInLowerCase = domainObjectType.toLowerCase();
            if (formConfig.isDefault()) {
                if (defaultFormByDomainObjectType.containsKey(domainObjectType)) {
                    throw new GuiException("There's more than 1 default form for type: " + domainObjectType);
                }
                defaultFormByDomainObjectType.put(domainObjectTypeInLowerCase, formConfig);
            }

            List<FormConfig> domainObjectTypeForms = allFormsByDomainObjectType.get(domainObjectType);
            if (domainObjectTypeForms == null) {
                domainObjectTypeForms = new ArrayList<>();
                allFormsByDomainObjectType.put(domainObjectTypeInLowerCase, domainObjectTypeForms);
            }
            domainObjectTypeForms.add(formConfig);
        }

        Collection<FormMappingConfig> formMappingConfigs = getFormMappingConfigs(configurationExplorer);
        for (FormMappingConfig formMapping : formMappingConfigs) {
            String domainObjectType = formMapping.getDomainObjectType();
            FormConfig formConfig = configurationExplorer.getConfig(FormConfig.class, formMapping.getForm());
            fillRoleAndDomainObjectTypeFormMappings(formMapping, domainObjectType, formConfig);
            fillUserAndDomainObjectTypeFormMappings(formMapping, domainObjectType, formConfig);
        }
    }

    private FormConfig getDefaultFormConfig(String domainObjectType) {
        return defaultFormByDomainObjectType.get(domainObjectType);
    }

    private List<FormConfig> getAllFormConfigs(String domainObjectType) {
        return allFormsByDomainObjectType.get(domainObjectType);
    }

    private List<FormConfig> getRoleFormConfigs(String roleName, String domainObjectType) {
        return formsByRoleAndDomainObjectType.get(new Pair<>(roleName, domainObjectType));
    }

    private List<FormConfig> getUserFormConfigs(String userUid, String domainObjectType) {
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
