package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.event.ConfigurationUpdateEvent;
import ru.intertrust.cm.core.config.gui.UserConfig;
import ru.intertrust.cm.core.config.gui.UsersConfig;
import ru.intertrust.cm.core.config.gui.navigation.GroupConfig;
import ru.intertrust.cm.core.config.gui.navigation.GroupsConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationPanelMappingConfig;
import ru.intertrust.cm.core.config.gui.navigation.NavigationPanelMappingsConfig;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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
        // <Имя группы пользователя, <имя панели навигации, приоритет>>
        private HashMap<String, Pair<String, Integer>> navigationsByUserGroup;
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
                int priority = groupConfig.getPriority() != null ? groupConfig.getPriority() : 0;
                navigationsByUserGroup.put(groupConfig.getName(), new Pair(navigationPanelMappingName, priority));
            }
        }
    }

    private NavigationConfig getNavigationPanelByUserGroup(List<DomainObject> userGroups) {
        Pair<String, Integer> maxPriorityPair = new Pair("", Integer.MIN_VALUE);
        for (DomainObject userGroup : userGroups) {
            String groupName = userGroup.getString("group_name");
            Pair<String, Integer> pair = navigationPanelsCache.navigationsByUserGroup.get(groupName);

            if (pair != null && pair.getSecond() > maxPriorityPair.getSecond()) {
                maxPriorityPair = pair;
            }

            List<DomainObject> allParentGroups = personManagementService.getAllParentGroup(personManagementService.getGroupId(groupName));
            for (DomainObject parentGroup : allParentGroups) {
                pair = navigationPanelsCache.navigationsByUserGroup.get(parentGroup.getString("group_name"));
                if (pair != null && pair.getSecond() > maxPriorityPair.getSecond()) {
                    maxPriorityPair = pair;
                }
            }
        }
        NavigationConfig navigationConfig = configurationExplorer.getConfig(NavigationConfig.class,
                maxPriorityPair.getFirst());

        return navigationConfig;
    }

    public NavigationConfig getNavigationPanel(String currentUser) {
        NavigationConfig navConfig = null;
        if (currentUser != null && !currentUser.isEmpty()) {
            navConfig = configurationExplorer.getConfig(NavigationConfig.class,
                    navigationPanelsCache.navigationsByUser.get(currentUser));
        }
        if (navConfig == null) {
            List<DomainObject> userGroups = personManagementService.getPersonGroups(personManagementService.getPersonId(currentUser));
            navConfig = getNavigationPanelByUserGroup(userGroups);
        }
        if (navConfig == null) {
            navConfig = navigationPanelsCache.getDefaultNavigationPanel();
        }
        return navConfig;
    }
}
