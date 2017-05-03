package ru.intertrust.cm.core.config.event;

import org.springframework.context.ApplicationEvent;
import ru.intertrust.cm.core.config.ConfigurationExplorer;
import ru.intertrust.cm.core.config.ConfigurationStorage;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.*;

/**
 * Событие обновления конфигурации
 */
public class ConfigurationUpdateEvent extends ApplicationEvent {

    private ConfigurationExplorer configurationExplorer;
    private ConfigurationStorage configurationStorage;

    private Set<ConfigChange> configChanges;
    private Map<Class<? extends TopLevelConfig>, HashSet<ConfigChange>> changedConfigurationsByClass;

    private boolean legacyDevelopmentMechanism;

    public ConfigurationUpdateEvent(ConfigurationExplorer configurationExplorer, ConfigurationStorage configStorage, TopLevelConfig oldConfig, TopLevelConfig newConfig) {
        super(configurationExplorer);

        this.configurationExplorer = configurationExplorer;
        this.configurationStorage = configStorage;

        this.configChanges = Collections.singleton(new ConfigChange(oldConfig, newConfig));

        this.legacyDevelopmentMechanism = true;
    }

    public ConfigurationUpdateEvent(ConfigurationExplorer source, Set<ConfigChange> configChanges) {
        super(source);
        this.configChanges = configChanges;
        this.legacyDevelopmentMechanism = false;
    }

    public boolean configTypeChanged(Class<? extends TopLevelConfig> clazz) {
        return getChangesByClass().containsKey(clazz);
    }

    public Set<String> getChangedConfigNames(Class<? extends TopLevelConfig> clazz) {
        final HashSet<ConfigChange> topLevelConfigs = getChangesByClass().get(clazz);
        if (topLevelConfigs == null) {
            return Collections.emptySet();
        }
        HashSet<String> result = new HashSet<>();
        for (ConfigChange topLevelConfig : topLevelConfigs) {
            result.add(topLevelConfig.getConfigName());
        }
        return result;
    }

    public Set<ConfigChange> getChangedConfigs(Class<? extends TopLevelConfig> clazz) {
        final HashSet<ConfigChange> configChanges = changedConfigurationsByClass.get(clazz);
        return configChanges == null ? Collections.<ConfigChange>emptySet() : configChanges;
    }

    public boolean containsChanges() {
        return !configChanges.isEmpty();
    }

    public boolean isLegacyDevelopmentMechanism() {
        return legacyDevelopmentMechanism;
    }

    @Deprecated
    public TopLevelConfig getOldConfig() {
        return configChanges.iterator().next().getBefore();
    }

    @Deprecated
    public TopLevelConfig getNewConfig() {
        return configChanges.iterator().next().getAfter();
    }

    @Deprecated
    public ConfigurationExplorer getConfigurationExplorer() {
        return configurationExplorer;
    }

    @Deprecated
    public ConfigurationStorage getConfigurationStorage() {
        return configurationStorage;
    }

    private Map<Class<? extends TopLevelConfig>, HashSet<ConfigChange>> getChangesByClass() {
        if (this.changedConfigurationsByClass != null) {
            return this.changedConfigurationsByClass;
        }
        this.changedConfigurationsByClass = new HashMap<>();
        for (ConfigChange configChange : configChanges) {
            HashSet<ConfigChange> changedConfigs = changedConfigurationsByClass.get(configChange.getConfigClass());
            if (changedConfigs == null) {
                changedConfigs = new HashSet<>();
                changedConfigurationsByClass.put(configChange.getConfigClass(), changedConfigs);
            }
            changedConfigs.add(configChange);
        }
        return this.changedConfigurationsByClass;
    }

}
