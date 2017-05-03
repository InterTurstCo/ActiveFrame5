package ru.intertrust.cm.core.config.event;

import ru.intertrust.cm.core.config.base.TopLevelConfig;

/**
 * @author Denis Mitavskiy
 *         Date: 02.05.2017
 *         Time: 19:28
 */
public class ConfigChange {
    private TopLevelConfig before;
    private TopLevelConfig after;
    private Class<? extends TopLevelConfig> configClass;
    private String configName;

    public ConfigChange(TopLevelConfig before, TopLevelConfig after) {
        if (before != null && after != null) {
            if (!before.getClass().equals(after.getClass())) {
                throw new IllegalArgumentException(before.getClass() + " doesn't match " + after.getClass());
            }
            if (!before.getName().equals(after.getName())) {
                throw new IllegalArgumentException("Config names are different: " + before.getName() + " vs " + after.getName());
            }

        }
        this.before = before;
        this.after = after;
        if (before == null) {
            this.configClass = after.getClass();
            this.configName = after.getName();
        } else {
            this.configClass = before.getClass();
            this.configName = after.getName();
        }
    }

    public TopLevelConfig getBefore() {
        return before;
    }

    public TopLevelConfig getAfter() {
        return after;
    }

    public Class<? extends TopLevelConfig> getConfigClass() {
        return configClass;
    }

    public String getConfigName() {
        return configName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConfigChange that = (ConfigChange) o;

        if (!configClass.equals(that.configClass)) return false;
        if (!configName.equals(that.configName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = configClass.hashCode();
        result = 31 * result + configName.hashCode();
        return result;
    }
}
