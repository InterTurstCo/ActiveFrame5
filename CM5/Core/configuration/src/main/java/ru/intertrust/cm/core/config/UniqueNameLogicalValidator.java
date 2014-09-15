package ru.intertrust.cm.core.config;

import org.simpleframework.xml.Root;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.intertrust.cm.core.config.base.Configuration;
import ru.intertrust.cm.core.config.base.TopLevelConfig;

import java.util.*;

/**
 * Валидатор уникальности имен конфигураций верхнего уровня одного типа
 */
public class UniqueNameLogicalValidator implements ConfigurationValidator {

    final static Logger logger = LoggerFactory.getLogger(UniqueNameLogicalValidator.class);

    private ConfigurationExplorer configurationExplorer;

    public UniqueNameLogicalValidator(ConfigurationExplorer configurationExplorer) {
        this.configurationExplorer = configurationExplorer;
    }

    /**
     * Выполняет логическую валидацию конфигурации
     */
    @Override
    public List<LogicalErrors> validate() {
        List<LogicalErrors> logicalErrorsList = new ArrayList<>();

        Set<Class<?>> topLevelConfigClasses = configurationExplorer.getTopLevelConfigClasses();
        if (topLevelConfigClasses == null || topLevelConfigClasses.isEmpty()) {
            return logicalErrorsList;
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
                    LogicalErrors logicalErrors = LogicalErrors.getInstance(config1.getName(), getTopLevelConfigTagName(config1));
                    logicalErrors.addError("There are top level configurations with identical name");
                    logicalErrorsList.add(logicalErrors);
                }
            }
        }

        return logicalErrorsList;
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

    private String getTopLevelConfigTagName(TopLevelConfig topLevelConfig) {
        Root root = topLevelConfig.getClass().getAnnotation(Root.class);
        if (root != null) {
            return root.name();
        } else {
            return topLevelConfig.getClass().getName();
        }
    }
}


