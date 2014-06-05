package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.event.ConfigurationUpdateEvent;
import ru.intertrust.cm.core.config.gui.UserConfig;
import ru.intertrust.cm.core.config.gui.UsersConfig;
import ru.intertrust.cm.core.config.gui.navigation.*;

import javax.annotation.PostConstruct;
import java.util.*;

/**
 * Created by IPetrov on 05.03.14.
 * Класс для определения конфигурации навигационной панели
 */
public class NavigationTreeResolver implements ApplicationListener<ConfigurationUpdateEvent> {
    private static Logger log = LoggerFactory.getLogger(NavigationTreeResolver.class);

    @Autowired
    private ConfigurationExplorer configurationExplorer;

    @Autowired
    private PersonManagementService personManagementService;

    private NavigationPanelsCache navigationPanelsCache;

    private NavigationTreeResolver() {
    }

    @PostConstruct
    private void initCaches() {
        navigationPanelsCache = new NavigationPanelsCache();
    }

    @Override
    public void onApplicationEvent(ConfigurationUpdateEvent event) {
       if(event.getNewConfig() instanceof NavigationConfig) {
           initCaches();
       }
    }

    private class NavigationPanelsCache {
        // Имя пользователя - имя панели навигации
        private HashMap<String, String> navigationsByUser;
        // Имя группы пользователя - имя панели навигации
        private HashMap<String, String> navigationsByUserGroup;
        private NavigationConfig defaultNavigationPanel;

        private NavigationPanelsCache() {
            navigationsByUser = new HashMap<>();
            navigationsByUserGroup = new HashMap<>();

            final Collection<NavigationConfig> navigationPanels = configurationExplorer.getConfigs(NavigationConfig.class);
            for (NavigationConfig config : navigationPanels) {
                if (config.isDefault()) {
                    defaultNavigationPanel = config;
                }
            }

            Collection<NavigationPanelMappingConfig> navigationPanelMappingConfigs = getNavigationPanelMappingConfigs(configurationExplorer);
            for (NavigationPanelMappingConfig navigationPanelMapping : navigationPanelMappingConfigs) {
                fillUserNavigationPanelMappings(navigationPanelMapping, navigationPanelMapping.getName());
                fillGroupNavigationPanelMappings(navigationPanelMapping, navigationPanelMapping.getName());
            }
        }

        public NavigationConfig getDefaultNavigationPanel() {
            return defaultNavigationPanel;
        }

        /*public NavigationConfig getNavigationPanelByUser(String userUid) {
            NavigationConfig navigationConfig = configurationExplorer.getConfig(NavigationConfig.class,
                                                                                navigationsByUser.get(userUid));
            return navigationConfig;
        }*/

        /*public NavigationConfig getNavigationPanelByUserGroup(String groupName) {
            NavigationConfig navigationConfig = configurationExplorer.getConfig(NavigationConfig.class,
                                                                                navigationsByUserGroup.get(groupName));
            return navigationConfig;
        }*/

        private Collection<NavigationPanelMappingConfig> getNavigationPanelMappingConfigs(ConfigurationExplorer explorer) {
            Collection<NavigationPanelMappingsConfig> configs = explorer.getConfigs(NavigationPanelMappingsConfig.class);
            if (configs == null || configs.isEmpty()) {
                return Collections.EMPTY_LIST;
            }
            ArrayList<NavigationPanelMappingConfig> result = new ArrayList<>();
            for (NavigationPanelMappingsConfig config : configs) {
                List<NavigationPanelMappingConfig> navigationPanelMappings = config.getNavigationPanelMappingConfigList();
                if (navigationPanelMappings != null) {
                    result.addAll(navigationPanelMappings);
                }
            }
            return result;
        }

        private void fillUserNavigationPanelMappings(NavigationPanelMappingConfig navigationPanelMappingConfig,
                                                                                     String navigationPanelMappingName) {
            UsersConfig usersConfig = navigationPanelMappingConfig.getUsersConfig();
            if (usersConfig == null) {
                return;
            }

            List<UserConfig> userConfigs = usersConfig.getUserConfigList();
            if (userConfigs == null || userConfigs.size() == 0) {
                return;
            }
            for (UserConfig userConfig : userConfigs) {
                String userUid = userConfig.getUid();
                navigationsByUser.put(userUid, navigationPanelMappingName);
            }
        }

        private void fillGroupNavigationPanelMappings(NavigationPanelMappingConfig navigationPanelMappingConfig,
                                                                                    String navigationPanelMappingName) {
            GroupsConfig groupsConfig = navigationPanelMappingConfig.getGroupsConfig();
            if (groupsConfig == null) {
                return;
            }

            List<GroupConfig> groupConfigs = groupsConfig.getGroupConfigList();
            if (groupConfigs == null || groupConfigs.size() == 0) {
                return;
            }
            for (GroupConfig groupConfig : groupConfigs) {
                String groupName = groupConfig.getName();
                navigationsByUserGroup.put(groupName, navigationPanelMappingName);
            }
        }
    }

    public NavigationConfig getNavigationPanelByUser(String userUid) {
        NavigationConfig navigationConfig = configurationExplorer.getConfig(NavigationConfig.class,
                navigationPanelsCache.navigationsByUser.get(userUid));
        if (navigationConfig == null) {
            return navigationPanelsCache.getDefaultNavigationPanel();
        }
        return navigationConfig;
    }

    public NavigationConfig getNavigationPanelByUserGroup(String groupName) {
        NavigationConfig navigationConfig = null;
        List<DomainObject> allParentGroups = personManagementService.getAllParentGroup(personManagementService.getGroupId(groupName));
        for (DomainObject parentGroup : allParentGroups) {
            navigationConfig = configurationExplorer.getConfig(NavigationConfig.class,
                               navigationPanelsCache.navigationsByUserGroup.get(parentGroup.getTypeName()/*groupName*/));
            if (navigationConfig != null)
                break;
        }
        return navigationConfig;
    }

    public NavigationConfig getNavigationPanel(String currentUser) {
        NavigationConfig navConfig = null;
        if (currentUser != null && !currentUser.isEmpty()) {
            navConfig = getNavigationPanelByUser(currentUser);
        } else {

            List<DomainObject> userGroups = personManagementService.getPersonGroups(personManagementService.getPersonId(currentUser));

            for (DomainObject userGroup : userGroups) {
                navConfig = getNavigationPanelByUserGroup(userGroup.getTypeName());
                if (navConfig != null)
                    break;
            }
        }
        return navConfig;
    }
}
