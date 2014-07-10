package ru.intertrust.cm.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.*;

/**
 * Валидатор уникальности имен конфигураций верхнего уровня одного типа
 */
public class UniqueNameLogicalValidator {

    final static Logger logger = LoggerFactory.getLogger(UniqueNameLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;

    public UniqueNameLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Выполняет логическую валидацию конфигурации
     */
    public void validate() {
        Set<Class<?>> topLevelConfigClasses = configurationExplorer.getTopLevelConfigClasses();
        if (topLevelConfigClasses == null || topLevelConfigClasses.isEmpty()) {
            return;
        }

        for (Class topLevelConfigClass : topLevelConfigClasses) {
            Collection configs = getConfigs(topLevelConfigClass);
            if (configs == null || configs.size() < 2) {
                continue;
            }

            List<TopLevelConfig> configsList = new ArrayList<TopLevelConfig>(configs);
            NameComparator nameComparator = new NameComparator();
            Collections.sort(configsList, nameComparator);

            for (int i = 1; i < configsList.size(); i ++) {
                TopLevelConfig config1 = configsList.get(i - 1);
                TopLevelConfig config2 = configsList.get(i);

                if (config1.getName() != null && nameComparator.compare(config1, config2) == 0) {
                    throw new ConfigurationException("There are top level configurations of type '" +
                            topLevelConfigClass.getName() + "' with identical name '" + config1.getName() + "'");
                }
            }
        }

        logger.info("Configuration has passed unique name logical validation");
    }

    private List<TopLevelConfig> getConfigs(Class clazz) {
        Configuration configuration = configurationExplorer.getConfiguration();
        List<TopLevelConfig> result = new LinkedList<>();

        for(TopLevelConfig config : configuration.getConfigurationList()) {
            if (config.getClass().equals(clazz)) {
                result.add(config);
            }
        }

        return result;
    }

    private class NameComparator implements Comparator<TopLevelConfig> {

        @Override
        public int compare(TopLevelConfig o1, TopLevelConfig o2) {
            if (o1.getName() == null && o2.getName() == null) {
                return 0;
            } else if (o1.getName() == null) {
                return -1;
            } else if (o2.getName() == null) {
                return 1;
            } else {
                return o1.getName().compareTo(o2.getName());
            }
        }
    }
}


