package ru.intertrust.cm.core.gui.impl.server.plugin.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import ru.intertrust.cm.core.business.api.PersonManagementService;
import ru.intertrust.cm.core.business.api.dto.DomainObject;
import ru.intertrust.cm.core.business.api.dto.Pair;
import ru.intertrust.cm.core.config.ConfigurationException;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.event.ConfigurationUpdateEvent;
import ru.intertrust.cm.core.config.gui.UserConfig;
import ru.intertrust.cm.core.config.gui.UsersConfig;
import ru.intertrust.cm.core.config.gui.navigation.*;
import ru.intertrust.cm.core.gui.api.server.GuiContext;

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
       if(event.configTypeChanged(NavigationConfig.class)) {
           initCaches();
       }
    }

    private class NavigationPanelsCache {
        // Имя пользователя - имя панели навигации
        private Map<String, String> navigationsByUser;
        // <Имя группы пользователя, <имя панели навигации, приоритет>>
        private Map<String, List<Pair<String, Integer>>> navigationsByUserGroup;
        private NavigationConfig defaultNavigationPanel;
        private Set<String> mergebleNavigationPanels;

        private NavigationPanelsCache() {
            navigationsByUser = new HashMap<>();
            navigationsByUserGroup = new HashMap<>();
            mergebleNavigationPanels = new HashSet<>();

            final Collection<NavigationConfig> navigationPanels = configurationExplorer.getConfigs(NavigationConfig.class);
            for (NavigationConfig navigation : navigationPanels) {
                if (navigation.isDefault()) {
                    defaultNavigationPanel = navigation;
                }
                if (navigation.isMerge()) {
                    mergebleNavigationPanels.add(navigation.getName());
                }
            }

            Collection<NavigationPanelMappingConfig> navigationPanelMappingConfigs = getNavigationPanelMappingConfigs(configurationExplorer);
            for (NavigationPanelMappingConfig navigationPanelMapping : navigationPanelMappingConfigs) {
                fillUserNavigationPanelMappings(navigationPanelMapping);
                fillGroupNavigationPanelMappings(navigationPanelMapping);
            }
        }

        public NavigationConfig getDefaultNavigationPanel() {
            return defaultNavigationPanel;
        }

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

        private void fillUserNavigationPanelMappings(NavigationPanelMappingConfig navigationPanelMappingConfig) {
            String navigationPanelMappingName = navigationPanelMappingConfig.getName();
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

        private void fillGroupNavigationPanelMappings(NavigationPanelMappingConfig navigationPanelMappingConfig) {
            String navigationPanelMappingName = navigationPanelMappingConfig.getName();
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
                List<Pair<String, Integer>> existingPairs = navigationsByUserGroup.get(groupConfig.getName());
                if (existingPairs == null) {
                    existingPairs = new ArrayList<>();
                    navigationsByUserGroup.put(groupConfig.getName(), existingPairs);
                }
                existingPairs.add(new Pair(navigationPanelMappingName, priority));
            }
        }
    }

    private NavigationConfig getNavigationPanelByUserGroup(List<DomainObject> userGroups) {
        List<Pair<String, Integer>> navigationPanelPairs = new ArrayList<>();
        for (DomainObject userGroup : userGroups) {
            String groupName = userGroup.getString("group_name");
            List<Pair<String, Integer>> pairs = navigationPanelsCache.navigationsByUserGroup.get(groupName);
            if (pairs != null) {
                navigationPanelPairs.addAll(pairs);
            }
        }
        if (navigationPanelPairs.isEmpty()) {
            return null;
        }

        boolean merge = true;
        for (Pair<String, Integer> pair : navigationPanelPairs) {
            String name = pair.getFirst();
            merge &= navigationPanelsCache.mergebleNavigationPanels.contains(name);
        }
        // sort by priority descending
        Collections.sort(navigationPanelPairs, new Comparator<Pair<String, Integer>>() {
            @Override
            public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
                return o2.getSecond().compareTo(o1.getSecond());
            }
        });

        if (!merge || navigationPanelPairs.size() == 1) {
            return getNavigationConfig(navigationPanelPairs, 0);
        } else {
            return mergeNavigationPanels(navigationPanelPairs);
        }
    }

    private NavigationConfig getNavigationConfig(List<Pair<String, Integer>> navigationPanelPairs, int i) {
        return getLocalizedNavigationConfig(navigationPanelPairs.get(i).getFirst());
    }

    private NavigationConfig mergeNavigationPanels(List<Pair<String, Integer>> navigationPanelPairs) {
        NavigationConfig main = getNavigationConfig(navigationPanelPairs, 0);
        List<LinkConfig> mergedLinks = main.getLinkConfigList();
        for (int i = 1; i < navigationPanelPairs.size(); i++) {
            NavigationConfig configToAdd = getNavigationConfig(navigationPanelPairs, i);
            mergeLinks(mergedLinks, configToAdd.getLinkConfigList());
        }
        return main;
    }

    private void mergeLinks(List<LinkConfig> primaryLinks, List<LinkConfig> secondaryLinks) {
        Map<String, LinkConfig> nameToLink = buildNameToLinkMap(secondaryLinks);

        for (LinkConfig linkConfig : primaryLinks) {
            LinkConfig linkToAdd = nameToLink.get(linkConfig.getName());
            if (linkToAdd != null) {
                if (linkConfig.getPluginDefinition() == null && linkToAdd.getPluginDefinition() != null ||
                        linkConfig.getPluginDefinition() != null
                                && !linkConfig.getPluginDefinition().equals(linkToAdd.getPluginDefinition())) {
                    throw new ConfigurationException("Ошибка при объединении навигационых панелей");
                }
                mergeChildLinks(linkConfig.getChildLinksConfigList(), linkToAdd.getChildLinksConfigList());
                nameToLink.remove(linkConfig.getName());
            }
        }
        primaryLinks.addAll(nameToLink.values());
    }

    private Map<String, LinkConfig> buildNameToLinkMap(List<LinkConfig> links) {
        Map<String, LinkConfig> nameToLink = new HashMap<>();
        for (LinkConfig linkConfig : links) {
            nameToLink.put(linkConfig.getName(), linkConfig);
        }
        return  nameToLink;
    }

    private void mergeChildLinks(List<ChildLinksConfig> primaryLinks, List<ChildLinksConfig> secondaryLinks) {
        Map<String, ChildLinksConfig> groupNameToChildLink = buildGroupNameToChildLinkMap(secondaryLinks);

        for (ChildLinksConfig linkConfig : primaryLinks) {
            ChildLinksConfig linkToAdd = groupNameToChildLink.get(linkConfig.getGroupName());
            if (linkToAdd != null) {

                mergeLinks(linkConfig.getLinkConfigList(), linkToAdd.getLinkConfigList());
                groupNameToChildLink.remove(linkConfig.getGroupName());
            }
        }
        primaryLinks.addAll(groupNameToChildLink.values());
    }

    private Map<String, ChildLinksConfig> buildGroupNameToChildLinkMap(List<ChildLinksConfig> links) {
        Map<String, ChildLinksConfig> groupNameToChildLink = new HashMap<>();
        for (ChildLinksConfig linkConfig : links) {
            groupNameToChildLink.put(linkConfig.getGroupName(), linkConfig);
        }
        return  groupNameToChildLink;
    }


    public NavigationConfig getNavigationPanel(String currentUser) {
        NavigationConfig navConfig = null;
        if (currentUser != null && !currentUser.isEmpty()) {
            navConfig = getLocalizedNavigationConfig(navigationPanelsCache.navigationsByUser.get(currentUser));
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



    private NavigationConfig getLocalizedNavigationConfig(String navConfigName) {
        return configurationExplorer.getLocalizedConfig(NavigationConfig.class, navConfigName, GuiContext.getUserLocale());
    }
}
